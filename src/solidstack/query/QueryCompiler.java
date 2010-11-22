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


public class QueryCompiler
{
	static final private Logger LOGGER = LoggerFactory.getLogger( QueryCompiler.class );

	static final protected Pattern pathPattern = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );

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

		GroovyClassLoader loader = new GroovyClassLoader();
		Class< GroovyObject > groovyClass = loader.parseClass( new GroovyCodeSource( script, name, "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		return new QueryTemplate( (Closure)object.invokeMethod( "getClosure", null ), lastModified );
	}

	static public QueryTemplate compile( String sql, String path, long lastModified )
	{
		return compile( new StringReader( sql ), path, lastModified );
	}


	static protected class Scanner
	{
//		static final private Logger log = Logger.getLogger( Scanner.class );

		protected Reader reader;
		protected Stack< Integer > pushBack = new Stack();
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

	static protected class Parser
	{
//		static final private Logger log = Logger.getLogger( GroovyPageCompiler.class );

		protected Parser()
		{
			// Constructor
		}

		protected String parse( Scanner scanner, String pkg, String cls )
		{
			Writer writer = new Writer( cls );
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
					if( c == '\\' )
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

		protected String readString( Scanner reader )
		{
//			log.trace( "-> readString" );
			StringBuilder result = new StringBuilder();
			boolean escaped = false;
			while( true )
			{
				int c = reader.read();
				Assert.isTrue( c > 0 );
				if( c == '"' && !escaped )
					return result.toString();
				escaped = c == '\\';
				if( !escaped )
					result.append( (char)c );
			}
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

	static protected enum Mode { UNKNOWN, STRING, SCRIPT, EXPRESSION }

	static protected class Writer
	{
		protected StringBuilder buffer = new StringBuilder();
		protected Mode mode = Mode.UNKNOWN;
		protected String cls;

		protected Writer()
		{
			// Empty constructor
		}

		protected Writer( String cls )
		{
			this.cls = cls;
		}

		protected void writeAsString( char c )
		{
			endAllExcept( Mode.STRING );
			if( this.mode == Mode.UNKNOWN )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.mode = Mode.STRING;
			}
			if( c == '\n' )
			{
				this.buffer.append( "\\n" );
				endAll();
			}
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
			else if( mode == Mode.SCRIPT )
				writeAsScript( c );
			else if( mode == Mode.STRING )
				writeAsString( c );
			else
				Assert.fail( "mode UNKNOWN not allowed" );
		}

		// TODO What about newlines?
		protected void writeAs( CharSequence string, Mode mode )
		{
			if( string == null || string.length() == 0 )
				return;

			if( mode == Mode.EXPRESSION )
				Assert.fail( "mode EXPRESSION not allowed" );
			else if( mode == Mode.SCRIPT )
				writeAsScript( string );
			else if( mode == Mode.STRING )
				writeWhiteSpaceAsString( string );
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
