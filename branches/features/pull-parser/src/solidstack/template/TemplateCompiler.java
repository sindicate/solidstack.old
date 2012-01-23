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

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;
import solidbase.io.StringLineReader;
import solidstack.Assert;
import solidstack.query.QueryNotFoundException;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;

/**
 * Template compiler.
 * 
 * @author René M. de Bloois
 */
public class TemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( TemplateCompiler.class );

	static final public Pattern PATH_PATTERN = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );
	static final public Pattern CONTENT_TYPE_PATTERN = Pattern.compile( "^[ \\t]*(\\S*)[ \\t]*(?:;[ \\t]*charset[ \\t]*=[ \\t]*(\\S*)[ \\t]*)?$" ); // TODO Improve
	static final public Pattern ENCODING_PATTERN = Pattern.compile( "^<%@[ \t]*template[ \t]+encoding[ \t]*=\"([^\"]*)\"[ \t]*%>[ \t]*$", Pattern.CASE_INSENSITIVE );



	/**
	 * Compiles a template into a {@link Template}.
	 * 
	 * @param reader The {@link Reader} for the template.
	 * @param path The path of the template.
	 * @param lastModified The last modified time stamp of the template.
	 * @return A {@link Template}.
	 */
	// TODO Use Resource instead of LineReader
	static public Template compile( Resource resource, String path, long lastModified )
	{
		log.info( "Compiling {}", resource );

		LineReader reader;
		try
		{
			reader = new BOMDetectingLineReader( resource, ENCODING_PATTERN );
		}
		catch( FileNotFoundException e )
		{
			throw new QueryNotFoundException( resource.toString() + " not found" );
		}

		log.info( "compile [{}]", path );
		Matcher matcher = PATH_PATTERN.matcher( path );
		Assert.isTrue( matcher.matches() );
		path = matcher.group( 1 );
		String name = matcher.group( 2 ).replaceAll( "[\\.-]", "_" );

		String pkg = "solidstack.template.tmp";
		if( path != null )
			pkg += "." + path.replaceAll( "/", "." );

		Template template = translate( pkg, name, reader );
		log.trace( "Generated groovy:\n{}", template.getSource() );

		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( template.getSource(), name, "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );

		template.setClosure( (Closure)object.invokeMethod( "getClosure", null ) );
		template.setLastModified( lastModified );
		template.clearSource();

		Directive d = template.getDirective( "template", "contentType" );
		if( d != null )
		{
			matcher = CONTENT_TYPE_PATTERN.matcher( d.getValue() );
			Assert.isTrue( matcher.matches(), "Couldn't interpret contentType " + d.getValue() );
			template.setContentType( matcher.group( 1 ) );
			template.setCharSet( matcher.group( 2 ) );
		}

		return template;
	}

	// TODO We should really have some kind of GroovyWriter which can do the escaping
	static void writeString( StringBuilder buffer, String s )
	{
		int len = s.length();
		for( int i = 0; i < len; i++ )
		{
			char c = s.charAt( i );
			if( c == '"' )
			{
				buffer.append( '\\' );
				buffer.append( c );
			}
			else
				buffer.append( c );
		}
	}

	static Template translate( String pkg, String cls, LineReader reader )
	{
		JSPLikeTemplateParser parser = new JSPLikeTemplateParser( reader );
		StringBuilder buffer = new StringBuilder();
		boolean text = false;
		List< String > imports = null;
		List< Directive > directives = null;
		loop: while( true )
		{
			ParseEvent event = parser.next();
			switch( event.getEvent() )
			{
				case TEXT:
				case NEWLINE:
				case WHITESPACE:
					if( !text )
						buffer.append( "writer.write(\"\"\"" );
					text = true;
					writeString( buffer, event.getData() );
					break;

				case SCRIPT:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() );
					buffer.append( ';' );
					break;

				case EXPRESSION:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( "writer.write(" );
					buffer.append( event.getData() );
					buffer.append( ");" );
					break;

				case EXPRESSION2:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( "writer.writeEncoded(" );
					buffer.append( event.getData() );
					buffer.append( ");" );
					break;

				case DIRECTIVE:
					if( directives == null )
						directives = new ArrayList< Directive >();
					directives.addAll( event.getDirectives() );

					for( Directive directive : event.getDirectives() )
						if( directive.getName().equals( "template" ) && directive.getAttribute().equals( "import" ) )
						{
							if( imports == null )
								imports = new ArrayList< String >();
							imports.add( directive.getValue() );
						}
					//$FALL-THROUGH$

				case COMMENT:
					if( event.getData().length() == 0 )
						break;
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() );
					break;

				case EOF:
					if( text )
						buffer.append( "\"\"\");" );
					break loop;

				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}
		}
		StringBuilder prelude = new StringBuilder( 256 );
		prelude.append( "package " );
		prelude.append( pkg );
		prelude.append( ";" );
		if( imports != null )
			for( String imprt : imports )
			{
				prelude.append( "import " );
				prelude.append( imprt );
				prelude.append( ';' );
			}
		prelude.append( "class " );
		prelude.append( cls );
		prelude.append( "{Closure getClosure(){return{writer->" );
		buffer.insert( 0, prelude );
		buffer.append( "}}}" );
		return new Template( buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
	}

	// For testing purposes
	static Template translate( String text )
	{
		return translate( "p", "c", new StringLineReader( text ) );
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
}
