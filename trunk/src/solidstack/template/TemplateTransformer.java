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

package solidstack.template;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.io.PushbackReader;
import solidstack.template.JSPLikeTemplateParser;
import solidstack.template.JSPLikeTemplateParser.ModalWriter;
import solidstack.template.ParseException;

/**
 * Translates a query template into a Groovy {@link Closure}.
 * 
 * @author René M. de Bloois
 */
public class TemplateTransformer
{
	static final private Logger LOGGER = LoggerFactory.getLogger( TemplateTransformer.class );

	static final private Pattern pathPattern = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );

	/**
	 * Compiles a template into a {@link Template}.
	 * 
	 * @param reader The {@link Reader} for the template text.
	 * @param path The path of the template.
	 * @param lastModified The last modified time stamp of the template.
	 * @return A {@link Template}.
	 */
	static public Template compile( Reader reader, String path, long lastModified )
	{
		LOGGER.info( "compile [" + path + "]" );
		Matcher matcher = pathPattern.matcher( path );
		Assert.isTrue( matcher.matches() );
		path = matcher.group( 1 );
		String name = matcher.group( 2 ).replaceAll( "[\\.-]", "_" );

		String pkg = "solidstack.template.tmp";
		if( path != null )
			pkg += "." + path.replaceAll( "/", "." );

		String script = new JSPLikeTemplateParser().parse( new PushbackReader( reader, 1 ), new Writer( pkg, name ) );
		if( LOGGER.isTraceEnabled() )
			LOGGER.trace( "Generated groovy:\n" + script );

		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, name, "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		return new Template( (Closure)object.invokeMethod( "getClosure", null ), lastModified );
	}

	/**
	 * Compiles a template into a {@link Template}.
	 * 
	 * @param template The text of the template.
	 * @param path The path of the  template.
	 * @param lastModified The last modified time stamp of the  template.
	 * @return A {@link Template}.
	 */
	static public Template compile( String template, String path, long lastModified )
	{
		return compile( new StringReader( template ), path, lastModified );
	}

	// For testing purposes
	static String translate( Reader reader )
	{
		return new JSPLikeTemplateParser().parse( new PushbackReader( reader, 1 ), new Writer( "p", "c" ) );
	}

	// For testing purposes
	static String translate( String text )
	{
		return translate( new StringReader( text ) );
	}

	// For testing purposes
	static String execute( String script, Map< String, ? > parameters )
	{
		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, "n", "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );
		if( parameters != null )
			closure.setDelegate( parameters );
		return closure.call().toString();
	}

	static class Writer extends ModalWriter
	{
		private String pckg;
		private String cls;
		private List< String > imports = new ArrayList< String >();

		public Writer( String pckg, String cls )
		{
			this.pckg = pckg;
			this.cls = cls;
		}

		@Override
		protected void directive( String name, String attribute, String value, int lineNumber )
		{
			if( !name.equals( "template" ) )
				throw new ParseException( "Only expecting template directives", lineNumber );
			if( !attribute.equals( "import" ) )
				throw new ParseException( "The query directive only allows import attributes", lineNumber );
			this.imports.add( value );
		}

		@Override
		protected void activateMode( Mode mode )
		{
			if( this.mode == mode )
				return;

			// TODO Use switch?
			if( this.mode == Mode.TEXT )
				append( "\"\"\");" );
			else if( this.mode == Mode.EXPRESSION )
				append( ");" );
			else if( this.mode == Mode.EXPRESSION2 )
				append( "));" );
			else if( this.mode == Mode.SCRIPT )
				append( ';' ); // Groovy does not understand: "...} builder.append(..." Need extra ; when coming from SCRIPT
			else
				Assert.fail( "Unknown mode " + this.mode );

			if( mode == Mode.TEXT )
				append( "writer.write(\"\"\"" );
			else if( mode == Mode.EXPRESSION )
				append( "writer.write(" );
			else if( mode == Mode.EXPRESSION2 )
				append( "writer.write(escape(" );
			else if( mode != Mode.SCRIPT )
				Assert.fail( "Unknown mode " + mode );

			this.nextMode = this.mode = mode;
		}

		@Override
		protected String getResult()
		{
			StringBuilder result = new StringBuilder();
			result.append( "package " );
			result.append( this.pckg );
			result.append( ";" );
			for( String imprt : this.imports )
			{
				result.append( "import " );
				result.append( imprt );
				result.append( ';' );
			}
			result.append( "class " );
			result.append( this.cls );
			result.append( "{Closure getClosure(){return{writer->" );
			result.append( super.getBuffer() );
			result.append( "}}}" );
			return result.toString();
		}
	}
}
