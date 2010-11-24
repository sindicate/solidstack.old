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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles a query template into a {@link Closure}.
 * 
 * @author René M. de Bloois
 */
public class QueryCompiler
{
	static final private Logger LOGGER = LoggerFactory.getLogger( QueryCompiler.class );

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

		String script = new Parser().parse( new Scanner( reader ), pkg, name );
		LOGGER.debug( "Generated groovy:\n" + script );

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
		return new Parser().parse( new Scanner( new StringReader( text ) ), "p", "c" );
	}


	static String execute( String script )
	{
		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, "n", "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );
		return closure.call().toString();
	}


	static private class Scanner
	{
//		static final private Logger log = Logger.getLogger( Scanner.class );

		protected Reader reader;
		protected Stack< Integer > pushBack = new Stack< Integer >();
		protected Stack< Integer > pushBackMarked;

		protected Scanner( Reader reader )
		{
			if( !reader.markSupported() )
				reader = new BufferedReader( reader );
			this.reader = reader;
		}

		protected int read()
		{
			if( this.pushBack.isEmpty() )
			{
				try
				{
					int c = this.reader.read();
					if( c == 13 )
					{
						c = this.reader.read();
						if( c != 10 )
						{
							unread( c );
							c = 10;
						}
					}
					return c;
				}
				catch( IOException e )
				{
					throw new SystemException( e );
				}
			}
			return this.pushBack.pop();
		}

		protected void unread( int c )
		{
			this.pushBack.push( c );
		}

		protected void mark( int readAheadLimit )
		{
			try
			{
				this.reader.mark( readAheadLimit );
			}
			catch( IOException e )
			{
				throw new SystemException( e );
			}
			this.pushBackMarked = (Stack)this.pushBack.clone();
		}

		protected void reset()
		{
			try
			{
				this.reader.reset();
			}
			catch( IOException e )
			{
				throw new SystemException( e );
			}
			this.pushBack = this.pushBackMarked;
			this.pushBackMarked = null;
		}

		protected String readWhitespace()
		{
			StringBuilder builder = new StringBuilder();
			int c = read();
			while( Character.isWhitespace( (char)c ) && c != '\n' )
			{
				builder.append( (char)c );
				c = read();
			}
			unread( c );
			return builder.toString();
		}
	}

	static private class Parser
	{
//		static final private Logger log = Logger.getLogger( GroovyPageCompiler.class );

		protected Parser()
		{
			// Constructor
		}

		protected String parse( Scanner scanner, String pkg, String cls )
		{
			Writer writer = new Writer();
			writer.writeRaw( "package " + pkg + ";class " + cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );

//			log.trace( "-> parse" );
			String leading = scanner.readWhitespace();
			int c = scanner.read();
			while( c != -1 )
			{
				if( c == '<' )
				{
					c = scanner.read();
					if( c == '%' )
					{
						scanner.mark( 2 );
						c = scanner.read();
						if( c == '=' )
						{
							writer.writeWhiteSpaceAsString( leading ); leading = null;
							readScript( scanner, writer, Mode.EXPRESSION );
						}
						else if( c == '-' && scanner.read() == '-' )
						{
							if( leading == null )
								readComment( scanner );
							else
							{
								readComment( scanner );
								String trailing = scanner.readWhitespace();
								c = scanner.read();
								if( (char)c == '\n' )
									// Comment on lines of his own, then ignore the lines.
									leading = scanner.readWhitespace();
								else
								{
									scanner.unread( c );
									writer.writeWhiteSpaceAsString( leading ); leading = null;
									writer.writeWhiteSpaceAsString( trailing );
								}
							}
						}
						else
						{
							scanner.reset();
							if( leading == null )
								readScript( scanner, writer, Mode.SCRIPT );
							else
							{
								Writer writer2 = new Writer();
								writer2.mode = Mode.UNKNOWN;
								readScript( scanner, writer2, Mode.SCRIPT );
								String trailing = scanner.readWhitespace();
								c = scanner.read();
								if( (char)c == '\n' )
								{
									// If script on lines of his own, add the whitespace and the newline to the script.
									writer.writeAsScript( leading ); leading = null;
									writer.writeAsScript( writer2.buffer );
									writer.writeAsScript( trailing );
									writer.writeAsScript( '\n' ); // Must not lose newlines
									leading = scanner.readWhitespace();
								}
								else
								{
									scanner.unread( c );
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
						scanner.unread( c );
					}
				}
				else
				{
					writer.writeWhiteSpaceAsString( leading );
					leading = null;
					if( c == '$' )
					{
						// TODO And without {}?
						c = scanner.read();
						if( c == '{' )
							readGStringExpression( scanner, writer );
						else
						{
							writer.writeAsString( '$' );
							scanner.unread( c );
						}
					}
					else if( c == '\\' )
					{
						c = scanner.read();
						if( c == '$' )
						{
							writer.writeAsString( '\\' );
							writer.writeAsString( '$' );
						}
						else
						{
							writer.writeAsString( '\\' );
							writer.writeAsString( '\\' );
							scanner.unread( c );
						}
					}
					else if( c == '"' )
					{
						writer.writeAsString( '\\' );
						writer.writeAsString( (char)c );
					}
					else if( c == '\n' )
					{
						writer.writeAsString( (char)c );
						leading = scanner.readWhitespace();
					}
					else
					{
						writer.writeAsString( (char)c );
					}
				}

				c = scanner.read();
			}
			writer.writeWhiteSpaceAsString( leading );

//			log.trace( "<- parse" );

			writer.endAll();

			writer.writeRaw( "return builder.toGString()}}}" );

			return writer.getString();
		}

		protected void readScript( Scanner reader, Writer writer, Mode mode )
		{
			Assert.isTrue( mode == Mode.SCRIPT || mode == Mode.EXPRESSION );

//			log.trace( "-> readScript" );
			while( true )
			{
				int c = reader.read();
				Assert.isTrue( c > 0 );
				if( c == '"' )
					readString( reader, writer, mode );
//				else if( c == '\'' )
//					readString( reader, writer, mode );
				else if( c == '%' )
				{
					c = reader.read();
					if( c == '>' )
						break;
					reader.unread( c );
					writer.writeAs( '%', mode );
				}
				else
					writer.writeAs( (char)c, mode );
			}
//			log.trace( "<- readScript" );
		}

		protected void readString( Scanner reader, Writer writer, Mode mode )
		{
//			log.trace( "-> readString" );
			writer.writeAs( '"', mode );
			boolean escaped = false;
			while( true )
			{
				int c = reader.read();
				Assert.isTrue( c > 0 );
				if( c == '"' && !escaped )
					break;
				escaped = c == '\\';
				writer.writeAs( (char)c, mode );
			}
			writer.writeAs( '"', mode );
//			log.trace( "<- readString" );
		}

		protected void readGStringExpression( Scanner reader, Writer writer )
		{
//			log.trace( "-> readEuh" );
			writer.writeAsString( '$' );
			writer.writeAsString( '{' );
			while( true )
			{
				int c = reader.read();
				Assert.isTrue( c > 0 );
				if( c == '}' )
					break;
				if( c == '"' )
					readString( reader, writer, Mode.STRING );
//				else if( c == '\'' ) TODO This is important to, for example '}'
//					readString( reader, writer, Mode.EXPRESSION2 );
				else
					writer.writeAsString( (char)c );
			}
			writer.writeAsString( '}' );
//			log.trace( "<- readEuh" );
		}

		protected void readComment( Scanner reader )
		{
//			log.trace( "-> readComment" );
			while( true )
			{
				int c = reader.read();
				Assert.isTrue( c > 0 );
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
