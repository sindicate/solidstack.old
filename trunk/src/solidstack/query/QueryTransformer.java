/*--
 * Copyright 2006 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.query;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.io.PushbackReader;

/**
 * Translates a query template into a Groovy {@link Closure}.
 * 
 * @author René M. de Bloois
 */
public class QueryTransformer
{
	static final private Logger LOGGER = LoggerFactory.getLogger( QueryTransformer.class );

	static final private Pattern pathPattern = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );

	/**
	 * Compiles a query template into a {@link Closure}.
	 * 
	 * @param reader The {@link Reader} for the query template text.
	 * @param path The path of the query template.
	 * @param lastModified The last modified time stamp of the query template.
	 * @return A {@link Closure}.
	 */
	static public QueryTemplate compile( Reader reader, String path, long lastModified )
	{
		LOGGER.info( "compile [" + path + "]" );
		Matcher matcher = pathPattern.matcher( path );
		Assert.isTrue( matcher.matches() );
		path = matcher.group( 1 );
		String name = matcher.group( 2 ).replaceAll( "[\\.-]", "_" );

		String pkg = "solidstack.query.tmp.gsql";
		if( path != null )
			pkg += "." + path.replaceAll( "/", "." );

		String script = new Parser().parse( new PushbackReader( reader, 1 ), pkg, name );
		if( LOGGER.isTraceEnabled() )
			LOGGER.trace( "Generated groovy:\n" + script );

		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, name, "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		return new QueryTemplate( (Closure)object.invokeMethod( "getClosure", null ), lastModified );
	}

	/**
	 * Compiles a query template into a {@link Closure}.
	 * 
	 * @param query The text of the query template.
	 * @param path The path of the query template.
	 * @param lastModified The last modified time stamp of the query template.
	 * @return A {@link Closure}.
	 */
	static public QueryTemplate compile( String query, String path, long lastModified )
	{
		return compile( new StringReader( query ), path, lastModified );
	}


	static String translate( Reader reader )
	{
		return new Parser().parse( new PushbackReader( reader, 1 ), "p", "c" );
	}


	static String translate( String text )
	{
		return new Parser().parse( new PushbackReader( new StringReader( text ), 1 ), "p", "c" );
	}


	static String execute( String script, Map< String, ? > parameters )
	{
		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, "n", "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );
		if( parameters != null )
			closure.setDelegate( parameters );
		return closure.call().toString();
	}


	static class Parser
	{
		protected String parse( PushbackReader reader, String pkg, String cls )
		{
			Writer writer = new Writer();
			writer.write( "package " + pkg + ";class " + cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );

			StringBuilder leading = readWhitespace( reader );
			while( true )
			{
				writer.setMode( Mode.TEXT );

				// We are in TEXT mode here.
				// Expecting <%, <%=, <%--, ${
				// " must be escaped because text is appended with a append("""...""")
				// \n must be detected because we want to read leading whitespace in a special way
				// Only <, $ and \ may be escaped

				int c = reader.read();
				if( c == -1 )
					break;

				if( c == '\\' )
				{
					writer.write( leading ); leading = null;
					int cc = reader.read();
					if( cc == '$' || cc == '\\' )
					{
						writer.write( (char)c );
						writer.write( (char)cc );
					}
					else if( cc == '<' )
						writer.write( (char)cc );
					else
						throw new TransformerException( "Only <, $ or \\ can be escaped", reader.getLineNumber() );
					continue;
				}

				if( c == '<' )
				{
					int cc = reader.read();
					if( cc != '%' )
					{
						writer.write( leading ); leading = null;
						writer.write( (char)c );
						reader.push( cc );
						continue;
					}

					reader.mark( 2 );
					c = reader.read();

					if( c == '=' )
					{
						writer.write( leading ); leading = null;
						writer.setMode( Mode.EXPRESSION );
						readScript( reader, writer );
						continue;
					}

					if( c == '-' && reader.read() == '-' )
					{
						writer.setMode( Mode.SCRIPT );
						if( leading == null )
							readComment( reader, writer );
						else
						{
							// Comment started with leading whitespace only
							readComment( reader, writer );
							StringBuilder trailing = readWhitespace( reader );
							c = reader.read();
							if( (char)c == '\n' )
							{
								writer.write( '\n' ); // Must not lose newlines
								leading = readWhitespace( reader ); // Comment on its own lines, ignore the lines totally
							}
							else
							{
								reader.push( c );
								writer.setMode( Mode.TEXT );
								writer.write( leading ); leading = null;
								writer.write( trailing );
							}
						}
						continue;
					}

					reader.reset();

					if( leading == null )
					{
						writer.setMode( Mode.SCRIPT );
						readScript( reader, writer );
					}
					else
					{
						// Script started with leading whitespace only, transform the script into a buffer
						Writer buffer = new Writer();
						readScript( reader, buffer );
						StringBuilder trailing = readWhitespace( reader );
						c = reader.read();
						if( (char)c == '\n' )
						{
							// Script on its own lines, leading and trailing whitespace are added to the script instead of the text
							writer.setMode( Mode.SCRIPT );
							writer.write( leading ); leading = null;
							writer.write( buffer.getBuffer() );
							writer.write( trailing );
							writer.write( '\n' ); // Must not lose newlines
							leading = readWhitespace( reader );
						}
						else
						{
							reader.push( c );
							writer.write( leading ); leading = null;
							writer.setMode( Mode.SCRIPT );
							writer.write( buffer.getBuffer() );
							writer.setMode( Mode.TEXT );
							writer.write( trailing );
						}
					}

					continue;
				}

				writer.write( leading ); leading = null;

				if( c == '$' )
				{
					// TODO And without {}?
					int cc = reader.read();
					if( cc == '{' )
						readGStringExpression( reader, writer );
					else
					{
						writer.write( (char)c );
						reader.push( cc );
					}
					continue;
				}

				if( c == '"' )
				{
					// Because we are in a """ string, we need to add escaping to a "
					writer.write( '\\' );
					writer.write( (char)c );
					continue;
				}

				if( c == '\n' )
				{
					// Newline, we need to read leading whitespace
					writer.write( (char)c );
					leading = readWhitespace( reader );
					continue;
				}

				writer.write( (char)c );
			}

			writer.setMode( Mode.TEXT );
			writer.write( leading );
			writer.setMode( Mode.SCRIPT );
			writer.write( "return builder.toGString()}}}" );

			return writer.getString();
		}

		protected StringBuilder readWhitespace( PushbackReader reader )
		{
			StringBuilder builder = new StringBuilder();
			int c = reader.read();
			while( Character.isWhitespace( (char)c ) && c != '\n' )
			{
				builder.append( (char)c );
				c = reader.read();
			}
			reader.push( c );
			return builder;
		}

		protected void readScript( PushbackReader reader, Writer writer )
		{
			Assert.isTrue( writer.pendingMode == Mode.SCRIPT || writer.pendingMode == Mode.EXPRESSION );

			// We are in SCRIPT/EXPRESSION mode here.
			// Expecting ", %>
			// %> within strings should not end the script

			while( true )
			{
				int c = reader.read();
				if( c < 0 )
					throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
				if( c == '"' )
					readString( reader, writer );
				else if( c == '%' )
				{
					c = reader.read();
					if( c == '>' )
						break;
					reader.push( c );
					writer.write( '%' );
				}
				else
					writer.write( (char)c );
			}
		}

		protected void readString( PushbackReader reader, Writer writer )
		{
			Assert.isTrue( writer.pendingMode == Mode.EXPRESSION || writer.pendingMode == Mode.SCRIPT || writer.pendingMode == Mode.TEXT, "Unexpected mode " + writer.pendingMode );

			// String can be read in any mode
			// Expecting $, " and \
			// " within ${} should not end this string
			// \ is used to escape $, " and itself

			writer.write( '"' );
			boolean multiline = false;
			reader.mark( 2 );
			if( reader.read() == '"' && reader.read() == '"' )
			{
				multiline = true;
				writer.write( '"' );
				writer.write( '"' );
			}
			else
				reader.reset();

			while( true )
			{
				int c = reader.read();

				if( multiline )
				{
					if( c < 0 )
						throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
				}
				else
					if( c < 0 || c == '\n' )
						throw new TransformerException( "Unexpected end of line", reader.getLineNumber() );

				if( c == '\\' )
				{
					writer.write( (char)c );
					c = reader.read();
					if( c == '$' || c == '\\' || c == '"'  )
						writer.write( (char)c );
					else
						throw new TransformerException( "Only \", $ or \\ can be escaped", reader.getLineNumber() );
					continue;
				}

				if( c == '$' )
				{
					// TODO And without {}?
					c = reader.read();
					if( c == '{' )
						readGStringExpression( reader, writer );
					else
					{
						writer.write( '$' );
						reader.push( c );
					}
					continue;
				}

				if( c == '"' )
				{
					if( !multiline )
						break;

					reader.mark( 2 );
					if( reader.read() == '"' && reader.read() == '"' )
						break;
					reader.reset();
					writer.write( (char)c );
					continue;
				}

				writer.write( (char)c );
			}

			writer.write( '"' );
			if( multiline )
			{
				writer.write( '"' );
				writer.write( '"' );
			}
		}

		// TODO Single quote lines
		// TODO Should we allow { } blocks within GString expressions?
		protected void readGStringExpression( PushbackReader reader, Writer writer )
		{
			// GStringExpressions can be read in any mode
			// Expecting }, "
			// } within a string should not end this expression

			writer.write( '$' );
			writer.write( '{' );
			while( true )
			{
				int c = reader.read();
				if( c < 0 || c == '\n' )
					throw new TransformerException( "Unexpected end of line", reader.getLineNumber() );
				if( c == '}' )
					break;
				if( c == '"' )
					readString( reader, writer );
				else
					writer.write( (char)c );
			}
			writer.write( '}' );
		}

		protected void readComment( PushbackReader reader, Writer writer )
		{
			// We are in SCRIPT mode (needed to transfer newlines from the comment to the script)
			// Expecting --%>, <%--, \n
			// Comments can be nested
			// Newlines are transferred to the script

			while( true )
			{
				int c = reader.read();
				if( c < 0 )
					throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
				if( c == '-' )
				{
					reader.mark( 10 );
					if( reader.read() == '-' && reader.read() == '%' && reader.read() == '>' )
						break;
					reader.reset();
				}
				else if( c == '<' )
				{
					reader.mark( 10 );
					if( reader.read() == '%' && reader.read() == '-' && reader.read() == '-' )
						readComment( reader, writer );
					else
						reader.reset();
				}
				else if( c == '\n' )
				{
					writer.write( '\n' );
				}
			}
		}
	}

	static private enum Mode { SCRIPT, TEXT, EXPRESSION }

	static class Writer
	{
		protected StringBuilder buffer = new StringBuilder();
		protected Mode mode = Mode.SCRIPT;
		protected Mode pendingMode = Mode.SCRIPT;

		protected void setMode( Mode mode )
		{
			this.pendingMode = mode;
		}

		protected void write( char c )
		{
			switchMode( this.pendingMode );
			this.buffer.append( c );
		}

		protected void write( CharSequence string )
		{
			if( string == null || string.length() == 0 )
				return;
			switchMode( this.pendingMode );
			this.buffer.append( string );
		}

		private void switchMode( Mode mode )
		{
			if( this.mode == mode )
				return;

			if( this.mode == Mode.TEXT )
				this.buffer.append( "\"\"\");" );
			else if( this.mode == Mode.EXPRESSION )
				this.buffer.append( ");" );
			else if( this.mode == Mode.SCRIPT )
			{
				// FIXME Groovy BUG:
				// Groovy does not understand: builder.append("""    """); } builder.append("""
				// We need extra ;
				// this.buffer.append( ';' );
			}
			else
				Assert.fail( "Unknown mode " + this.mode );

			if( mode == Mode.TEXT )
				this.buffer.append( "builder.append(\"\"\"" );
			else if( mode == Mode.EXPRESSION )
				this.buffer.append( "builder.append(" );
			else if( mode != Mode.SCRIPT )
				Assert.fail( "Unknown mode " + mode );

			this.mode = mode;
		}

		protected StringBuilder getBuffer()
		{
			return this.buffer;
		}

		protected String getString()
		{
			return this.buffer.toString();
		}
	}
}
