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


	static String translate( String text )
	{
		return new Parser().parse( new PushbackReader( new StringReader( text ), 1 ), "p", "c" );
	}


	static String execute( String script )
	{
		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, "n", "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );
		return closure.call().toString();
	}


	static private class Parser
	{
//		static final private Logger log = Logger.getLogger( GroovyPageCompiler.class );

		protected Parser()
		{
			// Constructor
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

		protected String parse( PushbackReader reader, String pkg, String cls )
		{
			Writer writer = new Writer();
			writer.writeRaw( "package " + pkg + ";class " + cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );

//			log.trace( "-> parse" );
			String leading = readWhitespace( reader );
			int c = reader.read();
			boolean escaped = false;
			while( c != -1 )
			{
				if( escaped )
				{
					if( c != '<' && c != '$' && c != '\\' )
						throw new TransformerException( "Only <, $ or \\ can be escaped", reader.getLineNumber() );
					writer.writeWhiteSpaceAsString( leading );
					leading = null;
					if( c != '<' )
						writer.writeAsString( '\\' );
					writer.writeAsString( (char)c );
					escaped = false;
				}
				else if( c == '\\' )
				{
					escaped = true;
				}
				else if( c == '<' )
				{
					c = reader.read();
					if( c == '%' )
					{
						reader.mark( 2 );
						c = reader.read();
						if( c == '=' )
						{
							writer.writeWhiteSpaceAsString( leading ); leading = null;
							readScript( reader, writer, Mode.EXPRESSION );
						}
						else if( c == '-' && reader.read() == '-' )
						{
							if( leading == null )
								readComment( reader );
							else
							{
								readComment( reader );
								String trailing = readWhitespace( reader );
								c = reader.read();
								if( (char)c == '\n' )
									// Comment on lines of his own, then ignore the lines.
									leading = readWhitespace( reader );
								else
								{
									reader.push( c );
									writer.writeWhiteSpaceAsString( leading ); leading = null;
									writer.writeWhiteSpaceAsString( trailing );
								}
							}
						}
						else
						{
							reader.reset();
							if( leading == null )
								readScript( reader, writer, Mode.SCRIPT );
							else
							{
								Writer writer2 = new Writer();
								writer2.mode = Mode.UNKNOWN;
								readScript( reader, writer2, Mode.SCRIPT );
								String trailing = readWhitespace( reader );
								c = reader.read();
								if( (char)c == '\n' )
								{
									// If script on lines of his own, add the whitespace and the newline to the script.
									writer.writeAsScript( leading ); leading = null;
									writer.writeAsScript( writer2.buffer );
									writer.writeAsScript( trailing );
									writer.writeAsScript( '\n' ); // Must not lose newlines
									leading = readWhitespace( reader );
								}
								else
								{
									reader.push( c );
									writer.writeWhiteSpaceAsString( leading ); leading = null;
									writer.writeAsScript( writer2.buffer );
									writer.writeWhiteSpaceAsString( trailing );
								}
							}
						}
					}
					else
					{
						writer.writeWhiteSpaceAsString( leading ); leading = null;
						writer.writeAsString( '<' );
						reader.push( c );
					}
				}
				else
				{
					writer.writeWhiteSpaceAsString( leading );
					leading = null;
					if( c == '$' )
					{
						// TODO And without {}?
						c = reader.read();
						if( c == '{' )
							readGStringExpression( reader, writer, Mode.STRING );
						else
						{
							writer.writeAsString( '$' );
							reader.push( c );
						}
					}
					else if( c == '"' ) // Because we are in a """ string, we need to add escaping
					{
						writer.writeAsString( '\\' );
						writer.writeAsString( (char)c );
					}
					else if( c == '\n' )
					{
						writer.writeAsString( (char)c );
						leading = readWhitespace( reader );
					}
					else
					{
						writer.writeAsString( (char)c );
					}
				}

				c = reader.read();
			}
			writer.writeWhiteSpaceAsString( leading );

//			log.trace( "<- parse" );

			writer.endAll();

			writer.writeRaw( "return builder.toGString()}}}" );

			return writer.getString();
		}

		protected void readScript( PushbackReader reader, Writer writer, Mode mode )
		{
			Assert.isTrue( mode == Mode.SCRIPT || mode == Mode.EXPRESSION );

//			log.trace( "-> readScript" );
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
//			log.trace( "<- readScript" );
		}

		protected void readString( PushbackReader reader, Writer writer, Mode mode )
		{
			Assert.isTrue( mode == Mode.EXPRESSION || mode == Mode.SCRIPT || mode == Mode.STRING, "Unexpected mode " + mode );

//			log.trace( "-> readString" );
			writer.writeAs( '"', mode );
			boolean escaped = false;
			while( true )
			{
				int c = reader.read();
				if( c < 0 )
					throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
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
							writer.writeAsString( '$' );
							reader.push( c );
						}
					}
					else if( c == '"' )
						break;
					else
					{
						escaped = c == '\\';
						writer.writeAs( (char)c, mode );
					}
				}
			}
			writer.writeAs( '"', mode );
//			log.trace( "<- readString" );
		}

		protected void readGStringExpression( PushbackReader reader, Writer writer, Mode mode )
		{
//			log.trace( "-> readEuh" );
			writer.writeAs( '$', mode );
			writer.writeAs( '{', mode );
			while( true )
			{
				int c = reader.read();
				if( c < 0 )
					throw new TransformerException( "Unexpected end of file", reader.getLineNumber() );
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
//			log.trace( "<- readEuh" );
		}

		protected void readComment( PushbackReader reader )
		{
//			log.trace( "-> readComment" );
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
						readComment( reader );
					else
						reader.reset();
				}
			}
//			log.trace( "<- readComment" );
		}
	}

	static private enum Mode { UNKNOWN, STRING, SCRIPT, EXPRESSION }

	static private class Writer
	{
		protected StringBuilder buffer = new StringBuilder();
		protected Mode mode = Mode.UNKNOWN;

		protected Writer()
		{
			// Empty constructor
		}

		protected void writeAsString( char c )
		{
			endAllExcept( Mode.STRING );
			if( this.mode == Mode.UNKNOWN )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.mode = Mode.STRING;
			}
//			if( c == '\n' ) By enabling this you get build.append()s for each line of SQL
//			{
//				this.buffer.append( "\\n" );
//				endAll();
//			}
			this.buffer.append( c );
		}

		protected void writeWhiteSpaceAsString( CharSequence string )
		{
			if( string == null || string.length() == 0 )
				return;

			endAllExcept( Mode.STRING );
			if( this.mode == Mode.UNKNOWN )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.mode = Mode.STRING;
			}
			this.buffer.append( string );
		}

		protected void writeAsExpression( char c )
		{
			endAllExcept( Mode.EXPRESSION );
			if( this.mode == Mode.UNKNOWN )
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
			if( this.mode == Mode.UNKNOWN )
				this.mode = Mode.SCRIPT;
			this.buffer.append( c );
		}

		// TODO What about newlines?
		protected void writeAsScript( CharSequence script )
		{
			if( script == null || script.length() == 0 )
				return;

			endAllExcept( Mode.SCRIPT );
			if( this.mode == Mode.UNKNOWN )
				this.mode = Mode.SCRIPT;
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
			else if( mode == Mode.STRING )
				writeAsString( c );
			else
				Assert.fail( "mode UNKNOWN not allowed" );
		}

		protected void writeRaw( String s )
		{
			this.buffer.append( s );
		}

		private void endExpression()
		{
			Assert.isTrue( this.mode == Mode.EXPRESSION );
			this.buffer.append( ");" );
			this.mode = Mode.UNKNOWN;
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
//			this.buffer.append( ';' );
			this.mode = Mode.UNKNOWN;
		}

		private void endString()
		{
			Assert.isTrue( this.mode == Mode.STRING );
			this.buffer.append( "\"\"\");" );
			this.mode = Mode.UNKNOWN;
		}

		private void endAllExcept( Mode mode )
		{
			if( this.mode == mode )
				return;

			if( this.mode == Mode.STRING )
				endString();
			else if( this.mode == Mode.EXPRESSION )
				endExpression();
//			else if( this.mode == Mode.EXPRESSION2 )
//				endExpression2();
			else if( this.mode == Mode.SCRIPT )
				endScript();
			else if( this.mode != Mode.UNKNOWN )
				Assert.fail( "Unknown mode " + this.mode );
		}

		protected void endAll()
		{
			endAllExcept( null );
		}

		protected String getString()
		{
			return this.buffer.toString();
		}
	}
}
