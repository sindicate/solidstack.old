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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;
import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;

/**
 * Template compiler.
 * 
 * @author René M. de Bloois
 */
// TODO Gstring as (query) parameter
// TODO Out.write gstring in queries
// TODO SQL array parameter in queries
// TODO Include during runtime or compiletime
// TODO Scriptonly: (whole template is just <% fjlkj%>) check no newlines?
public class TemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( TemplateCompiler.class );

	static final private Pattern PATH_PATTERN = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );
	static final private Pattern CONTENT_TYPE_PATTERN = Pattern.compile( "^[ \\t]*(\\S*)[ \\t]*(?:;[ \\t]*charset[ \\t]*=[ \\t]*(\\S*)[ \\t]*)?$" ); // TODO case sensitive & http://www.iana.org/assignments/media-types/index.html
	static final private Pattern ENCODING_PATTERN = Pattern.compile( "^<%@[ \t]*template[ \t]+encoding[ \t]*=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE ); // TODO Improve, case sensitive?

	static boolean keepSource = false;



	/**
	 * Compiles a template into a {@link Template}.
	 * 
	 * @param resource The {@link Resource} that contains the template.
	 * @param path The path of the template, needed to generate a name for the class in memory.
	 * @return A {@link Template}.
	 */
	public Template compile( Resource resource, String path )
	{
		log.info( "Compiling {}", resource );

		LineReader reader;
		try
		{
			reader = new BOMDetectingLineReader( resource, ENCODING_PATTERN );
		}
		catch( FileNotFoundException e )
		{
			throw new TemplateNotFoundException( resource.toString() + " not found" );
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
		if( !keepSource )
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

	/**
	 * Writes a string with escaping of sensitive characters ", \ and $.
	 * 
	 * @param buffer The buffer to write to.
	 * @param s The string to write.
	 */
	// TODO We should really have some kind of GroovyWriter which can do the escaping
	static protected void writeString( StringBuilder buffer, String s )
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		char c;
		for( int i = 0; i < len; i++ )
			switch( c = chars[ i ] )
			{
				case '"':
				case '\\':
				case '$':
					buffer.append( '\\' ); //$FALL-THROUGH$
				default:
					buffer.append( c );
			}
	}

	/**
	 * Translates the template text to source code of the desired programming language.
	 * 
	 * @param pkg The package for naming the class.
	 * @param cls The class name.
	 * @param reader The reader to read the template text.
	 * @return The translated template.
	 */
	protected Template translate( String pkg, String cls, LineReader reader )
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
						buffer.append( "out.write(\"\"\"" );
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
					buffer.append( "out.write(" );
					buffer.append( event.getData() );
					buffer.append( ");" );
					break;

				case EXPRESSION2:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( "out.writeEncoded(" );
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
		prelude.append( "{Closure getClosure(){return{out->" );
		buffer.insert( 0, prelude );
		buffer.append( "}}}" );
		return new Template( buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
	}
}
