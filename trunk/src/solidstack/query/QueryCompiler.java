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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

		String script = parse( reader, pkg, name );
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

	static public String parse( Reader reader, String pkg, String cls )
	{
		Writer writer = new Writer();

		writer.write( "package " + pkg + ";class " + cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );

		try
		{
			int c = reader.read();
			while( true )
			{
				if( c == -1 )
					break;
				if( c == '<' )
				{
					c = reader.read();
					if( c == '%' )
					{
						c = reader.read();
						if( c == '=' )
						{
							writer.startExpression();
							readScript( reader, writer );
							writer.endExpression();
						}
						else
						{
							writer.startScript();
							writer.write( (char)c );
							readScript( reader, writer );
							writer.endScript();
						}
					}
					else
					{
						writer.writeAsString( '<' );
						writer.writeAsString( (char)c );
					}
				}
				else if( c == '\\' )
				{
					c = reader.read();
					if( c == '$' )
					{
						writer.writeAsString( '\\' );
						writer.writeAsString( '$' );
					}
					else
					{
						writer.writeAsString( '\\' );
						writer.writeAsString( '\\' );
						writer.writeAsString( (char)c );
					}
				}
				else if( c == '"' )
				{
					c = reader.read();
					if( c == '"' )
					{
						c = reader.read();
						if( c == '"' )
						{
							writer.writeAsString( '\\' );
							writer.writeAsString( '"' );
							writer.writeAsString( '"' );
							writer.writeAsString( '"' );
						}
						else
						{
							writer.writeAsString( '"' );
							writer.writeAsString( '"' );
							writer.writeAsString( (char)c );
						}
					}
					else
					{
						writer.writeAsString( '"' );
						writer.writeAsString( (char)c );
					}
				}
				else
					writer.writeAsString( (char)c );

				c = reader.read();
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}

		writer.end();

		writer.write( "return builder.toGString()}}}" );

		return writer.getString();
	}

	static protected void readScript( Reader reader, Writer writer ) throws IOException
	{
		while( true )
		{
			int c = reader.read();
			if( c == -1 )
				return;
			if( c == '%' )
			{
				c = reader.read();
				if( c == '>' )
					return;
				writer.write( '%' );
			}
			writer.write( (char)c );
		}
	}

	static protected class Writer
	{
		protected StringBuilder buffer;
		protected boolean stringMode = false;
		protected Writer()
		{
			this.buffer = new StringBuilder();
		}
		protected void writeAsString( char string )
		{
			if( !this.stringMode )
			{
				this.buffer.append( "builder.append(\"\"\"" );
				this.stringMode = true;
			}
			this.buffer.append( string );
		}
		protected void write( String s )
		{
			this.buffer.append( s );
		}
		protected void write( char c )
		{
			this.buffer.append( c );
		}
		protected void startExpression()
		{
			endString();
			this.buffer.append( "builder.append(" );
		}
		protected void endExpression()
		{
			this.buffer.append( ");" );
		}
		protected void startScript()
		{
			endString();
		}
		protected void endScript()
		{
			this.buffer.append( ';' );
		}
		protected void endString()
		{
			if( this.stringMode )
			{
				this.buffer.append( "\"\"\");" );
				this.stringMode = false;
			}
		}
		protected void end()
		{
			endString();
		}
		protected String getString()
		{
			return this.buffer.toString();
		}
	}
}
