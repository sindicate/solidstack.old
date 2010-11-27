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


	static String execute( String script, Map parameters )
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
			writer.writeAsScript( "package " + pkg + ";class " + cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );

			String leading = readWhitespace( reader );
			while( true )
			{
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
					writer.writeAsText( leading ); leading = null;
					int cc = reader.read();
					if( cc == '$' || cc == '\\' )
					{
						writer.writeAsText( (char)c );
						writer.writeAsText( (char)cc );
					}
					else if( cc == '<' )
						writer.writeAsText( (char)cc );
					else
						throw new TransformerException( "Only <, $ or \\ can be escaped", reader.getLineNumber() );
					continue;
				}

				if( c == '<' )
				{
					int cc = reader.read();
					if( cc != '%' )
					{
						writer.writeAsText( leading ); leading = null;
						writer.writeAsText( (char)c );
						reader.push( cc );
						continue;
					}

					reader.mark( 2 );
					c = reader.read();

					if( c == '=' )
					{
						writer.writeAsText( leading ); leading = null;
						readScript( reader, writer, Mode.EXPRESSION );
						continue;
					}

					if( c == '-' && reader.read() == '-' )
					{
						if( leading == null )
							readComment( reader, writer );
						else
						{
							// FIXME We are missing newlines here
							// Comment started with leading whitespace only
							readComment( reader, writer );
							String trailing = readWhitespace( reader );
							c = reader.read();
							if( (char)c == '\n' )
							{
								writer.writeAsScript( '\n' ); // Must not lose newlines
								leading = readWhitespace( reader ); // Comment on its own lines, ignore the lines totally
							}
							else
							{
								reader.push( c );
								writer.writeAsText( leading ); leading = null;
								writer.writeAsText( trailing );
							}
						}
						continue;
					}

					reader.reset();

					if( leading == null )
						readScript( reader, writer, Mode.SCRIPT );
					else
					{
						// Script started with leading whitespace only, transform the script into a buffer
						Writer buffer = new Writer();
						readScript( reader, buffer, Mode.SCRIPT );
						String trailing = readWhitespace( reader );
						c = reader.read();
						if( (char)c == '\n' )
						{
							// Script on its own lines, leading and trailing whitespace are added to the script instead of the text
							writer.writeAsScript( leading ); leading = null;
							writer.writeAsScript( buffer.getBuffer() );
							writer.writeAsScript( trailing );
							writer.writeAsScript( '\n' ); // Must not lose newlines
							leading = readWhitespace( reader );
						}
						else
						{
							reader.push( c );
							writer.writeAsText( leading ); leading = null;
							writer.writeAsScript( buffer.getBuffer() );
							writer.writeAsText( trailing );
						}
					}

					continue;
				}

				writer.writeAsText( leading ); leading = null;

				if( c == '$' )
				{
					// TODO And without {}?
					int cc = reader.read();
					if( cc == '{' )
						readGStringExpression( reader, writer, Mode.TEXT );
					else
					{
						writer.writeAsText( (char)c );
						reader.push( cc );
					}
					continue;
				}

				if( c == '"' )
				{
					// Because we are in a """ string, we need to add escaping to a "
					writer.writeAsText( '\\' );
					writer.writeAsText( (char)c );
					continue;
				}

				if( c == '\n' )
				{
					// Newline, we need to read leading whitespace
					writer.writeAsText( (char)c );
					leading = readWhitespace( reader );
					continue;
				}

				writer.writeAsText( (char)c );
			}

			writer.writeAsText( leading );
			writer.writeAsScript( "return builder.toGString()}}}" );

			return writer.getString();
		}

		protected String readWhitespace( PushbackReader reader )
		{
			StringBuilder builder = new StringBuilder();
			int c = reader.read();
			while( Character.isWhitespace( (char)c ) && c != '\n' )
			{
				builder.append( (char)c );
				c = reader.read();
			}
			reader.push( c );
			return builder.toString();
		}

		protected void readScript( PushbackReader reader, Writer writer, Mode mode )
		{
			Assert.isTrue( mode == Mode.SCRIPT || mode == Mode.EXPRESSION );

			while( true )
			{
				int c = reader.read();
				if( c < 0 )
					throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
				if( c == '"' )
					readString( reader, writer, mode );
//				else if( c == '\'' )
//					readString( reader, writer, mode );
				else if( c == '%' )
				{
					c = reader.read();
					if( c == '>' )
						break;
					reader.push( c );
					writer.writeAs( '%', mode );
				}
				else
					writer.writeAs( (char)c, mode );
			}
		}

		protected void readString( PushbackReader reader, Writer writer, Mode mode )
		{
			Assert.isTrue( mode == Mode.EXPRESSION || mode == Mode.SCRIPT || mode == Mode.TEXT, "Unexpected mode " + mode );

			writer.writeAs( '"', mode );
			boolean multiline = false;
			reader.mark( 2 );
			if( reader.read() == '"' && reader.read() == '"' )
			{
				multiline = true;
				writer.writeAs( '"', mode );
				writer.writeAs( '"', mode );
			}
			else
				reader.reset();

			boolean escaped = false;
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
				if( escaped )
				{
					escaped = false;
					writer.writeAs( (char)c, mode );
				}
				else
				{
					if( c == '$' )
					{
						// TODO And without {}?
						c = reader.read();
						if( c == '{' )
							readGStringExpression( reader, writer, mode );
						else
						{
							writer.writeAsText( '$' );
							reader.push( c );
						}
					}
					else if( c == '"' )
					{
						if( multiline )
						{
							reader.mark( 2 );
							if( reader.read() == '"' && reader.read() == '"' )
								break;
							reader.reset();
							writer.writeAs( (char)c, mode );
						}
						else
							break;
					}
					else
					{
						escaped = c == '\\';
						writer.writeAs( (char)c, mode );
					}
				}
			}

			writer.writeAs( '"', mode );
			if( multiline )
			{
				writer.writeAs( '"', mode );
				writer.writeAs( '"', mode );
			}
		}

		protected void readGStringExpression( PushbackReader reader, Writer writer, Mode mode )
		{
			writer.writeAs( '$', mode );
			writer.writeAs( '{', mode );
			while( true )
			{
				int c = reader.read();
				if( c < 0 || c == '\n' )
					throw new TransformerException( "Unexpected end of line", reader.getLineNumber() );
				if( c == '}' )
					break;
				if( c == '"' )
					readString( reader, writer, mode );
//				else if( c == '\'' ) TODO This is important to, for example '}'
//					readString( reader, writer, Mode.EXPRESSION2 );
				else
					writer.writeAs( (char)c, mode );
			}
			writer.writeAs( '}', mode );
		}

		protected void readComment( PushbackReader reader, Writer writer )
		{
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
					writer.writeAsScript( '\n' );
				}
			}
		}
	}

	static private enum Mode { SCRIPT, TEXT, EXPRESSION }

	static private class Writer
	{
		protected StringBuilder buffer = new StringBuilder();
		protected Mode mode = Mode.SCRIPT;

		protected Writer()
		{
			// Empty constructor
		}

		protected void writeAsText( char c )
		{
			endAllExcept( Mode.TEXT );
			if( this.mode == Mode.SCRIPT )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.mode = Mode.TEXT;
			}
//			if( c == '\n' ) By enabling this you get build.append()s for each line of SQL
//			{
//				this.buffer.append( "\\n" );
//				endAll();
//			}
			this.buffer.append( c );
		}

		protected void writeAsText( CharSequence string )
		{
			if( string == null || string.length() == 0 )
				return;

			endAllExcept( Mode.TEXT );
			if( this.mode == Mode.SCRIPT )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.mode = Mode.TEXT;
			}
			this.buffer.append( string );
		}

		protected void writeAsExpression( char c )
		{
			endAllExcept( Mode.EXPRESSION );
			if( this.mode == Mode.SCRIPT )
			{
				this.buffer.append( "builder.append(" );
				this.mode = Mode.EXPRESSION;
			}
			this.buffer.append( c );
		}

//		protected void writeAsExpression2( char c )
//		{
//			endAllExcept( Mode.EXPRESSION2 );
//			if( this.mode == Mode.UNKNOWN )
//			{
//				this.buffer.append( "writer.writeEncoded(" );
//				this.mode = Mode.EXPRESSION2;
//			}
//			this.buffer.append( c );
//		}

		protected void writeAsScript( char c )
		{
			endAllExcept( Mode.SCRIPT );
			this.buffer.append( c );
		}

		// TODO What about newlines?
		protected void writeAsScript( CharSequence script )
		{
			if( script == null || script.length() == 0 )
				return;

			endAllExcept( Mode.SCRIPT );
			this.buffer.append( script );
		}

		protected void writeAs( char c, Mode mode )
		{
			if( mode == Mode.EXPRESSION )
				writeAsExpression( c );
//			else if( mode == Mode.EXPRESSION2 )
//				writeAsExpression2( c );
			else if( mode == Mode.SCRIPT )
				writeAsScript( c );
			else if( mode == Mode.TEXT )
				writeAsText( c );
			else
				Assert.fail( "mode UNKNOWN not allowed" );
		}

		private void endExpression()
		{
			Assert.isTrue( this.mode == Mode.EXPRESSION );
			this.buffer.append( ");" );
			this.mode = Mode.SCRIPT;
		}

//		private void endExpression2()
//		{
//			Assert.isTrue( this.mode == Mode.EXPRESSION2 );
//			this.buffer.append( ");" );
//			this.mode = Mode.UNKNOWN;
//		}

		private void endScript()
		{
			Assert.isTrue( this.mode == Mode.SCRIPT );
			// FIXME Groovy BUG:
			// Groovy does not understand: builder.append("""    """); } builder.append("""
			// We need extra ;
//			this.buffer.append( ';' );
		}

		private void endString()
		{
			Assert.isTrue( this.mode == Mode.TEXT );
			this.buffer.append( "\"\"\");" );
			this.mode = Mode.SCRIPT;
		}

		private void endAllExcept( Mode mode )
		{
			if( this.mode == mode )
				return;

			if( this.mode == Mode.TEXT )
				endString();
			else if( this.mode == Mode.EXPRESSION )
				endExpression();
//			else if( this.mode == Mode.EXPRESSION2 )
//				endExpression2();
			else if( this.mode == Mode.SCRIPT )
				endScript();
			else
				Assert.fail( "Unknown mode " + this.mode );
		}

		protected void endAll()
		{
			endAllExcept( null );
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
