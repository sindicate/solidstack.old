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
import solidstack.template.JSPLikeTemplateParser;

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

		String script = new JSPLikeTemplateParser().parse( new PushbackReader( reader, 1 ), new Writer( pkg, name ) );
		if( LOGGER.isTraceEnabled() )
			LOGGER.trace( "Generated groovy:\n" + script );
		System.out.println( script );

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
		return new JSPLikeTemplateParser().parse( new PushbackReader( reader, 1 ), new Writer( "p", "c" ) );
	}


	static String translate( String text )
	{
		return new JSPLikeTemplateParser().parse( new PushbackReader( new StringReader( text ), 1 ), new Writer( "p", "c" ) );
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


	static class Writer extends solidstack.template.JSPLikeTemplateParser.Writer
	{
		private String pckg;
		private String cls;

		public Writer( String pckg, String cls )
		{
			this.pckg = pckg;
			this.cls = cls;
		}

		public Writer( Mode mode )
		{
			super( mode );
		}

		@Override
		protected void switchMode( Mode mode )
		{
			if( this.mode == mode )
				return;

			if( this.mode == Mode.INITIAL )
				this.buffer.append( "package " + this.pckg + ";class " + this.cls + "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );
			else if( this.mode == Mode.TEXT )
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

			this.nextMode = this.mode = mode;
		}

		@Override
		protected solidstack.template.JSPLikeTemplateParser.Writer newWriter( Mode mode )
		{
			return new Writer( mode );
		}
	}
}
