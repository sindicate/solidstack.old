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
import solidstack.template.JSPLikeTemplateParser.EVENT;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.groovy.GroovyTemplate;
import solidstack.template.javascript.JavaScriptTemplate;

/**
 * Template compiler.
 * 
 * @author René M. de Bloois
 */
// TODO Out.write gstring in queries? Act as JDBC bind parameters or not?
// TODO SQL array parameter in queries
// TODO Include during runtime or compiletime
// TODO Scriptonly: (whole template is just <% fjlkj%>) check no newlines?
// TODO Put the Groovy compiler in the groovy package, and the javascript compiler in the javascript package
public class TemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( TemplateCompiler.class );

	// TODO Is this pattern correct?
	static final private Pattern PATH_PATTERN = Pattern.compile( "/*(?:(.+?)/+)?([^\\/]+)" );
	static final private Pattern CONTENT_TYPE_PATTERN = Pattern.compile( "^[ \\t]*(\\S*)[ \\t]*(?:;[ \\t]*charset[ \\t]*=[ \\t]*(\\S*)[ \\t]*)?$" ); // TODO case sensitive & http://www.iana.org/assignments/media-types/index.html
	static final private Pattern ENCODING_PATTERN = Pattern.compile( "^<%@[ \t]*template[ \t]+encoding[ \t]*=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE ); // TODO Improve, case sensitive?

	static boolean keepSource = false;

	private TemplateManager manager;


	/**
	 * Constructor.
	 * 
	 * @param manager The template manager that created this compiler.
	 */
	public TemplateCompiler( TemplateManager manager )
	{
		this.manager = manager;
	}

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
		template.compile();
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
	 * Translates the template text to source code of the desired programming language.
	 * 
	 * @param pkg The package for naming the class.
	 * @param cls The class name.
	 * @param reader The reader to read the template text.
	 * @return The translated template.
	 */
	public Template translate( String pkg, String cls, LineReader reader ) // TODO Remove public
	{
		// Parse and collect directives
		JSPLikeTemplateParser parser = new JSPLikeTemplateParser( reader );
		List< ParseEvent > events = new ArrayList< ParseEvent >();
		List< Directive > directives = null;
		ParseEvent event = parser.next();
		while( event.getEvent() != EVENT.EOF )
		{
			events.add( event );
			if( event.getEvent() == EVENT.DIRECTIVE )
			{
				if( directives == null )
					directives = new ArrayList< Directive >();
				directives.addAll( event.getDirectives() );
			}
			event = parser.next();
		}

		// Collect imports & language
		List< String > imports = null;
		String lang = null;
		if( directives != null )
			for( Directive directive : directives )
				if( directive.getName().equals( "template" ) )
					if( directive.getAttribute().equals( "import" ) )
					{
						if( imports == null )
							imports = new ArrayList< String >();
						imports.add( directive.getValue() );
					}
					else if( directive.getAttribute().equals( "language" ) )
						lang = directive.getValue();

		if( lang == null )
		{
			if( this.manager != null )
			{
				lang = this.manager.getDefaultLanguage();
				if( lang == null )
					throw new TemplateException( "Template has no \"language\" directive, and no defaultLanguage configured in the TemplateManager" );
			}
			else
				throw new TemplateException( "Template has no \"language\" directive" );
		}

		if( lang.equals( "javascript" ) )
			return toJavaScript( pkg + "." + cls, events, directives, imports ); // TODO cls is not right

		if( lang.equals( "groovy" ) )
			return toGroovy( pkg, cls, events, directives, imports );

		throw new TemplateException( "Unsupported scripting language: " + lang );
	}

	// TODO Any other characters?
	static private void writeGroovyString( StringBuilder buffer, String s )
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

	static private GroovyTemplate toGroovy( String pkg, String cls, List< ParseEvent > events, List< Directive > directives, List< String > imports )
	{
		StringBuilder buffer = new StringBuilder( 1024 );
		buffer.append( "package " ).append( pkg ).append( ";" );
		if( imports != null )
			for( String imprt : imports )
				buffer.append( "import " ).append( imprt ).append( ';' );
		buffer.append( "class " ).append( cls );
		buffer.append( "{Closure getClosure(){return{out->" );

		boolean text = false;
		for( ParseEvent event : events )
			switch( event.getEvent() )
			{
				case TEXT:
				case NEWLINE:
				case WHITESPACE:
					if( !text )
						buffer.append( "out.write(\"\"\"" );
					text = true;
					writeGroovyString( buffer, event.getData() );
					break;

				case SCRIPT:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() ).append( ';' );
					break;

				case EXPRESSION:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( "out.write(" ).append( event.getData() ).append( ");" );
					break;

				case EXPRESSION2:
					if( !text )
						buffer.append( "out.write(\"\"\"" );
					text = true;
					buffer.append( "${" ).append( event.getData() ).append( '}' );
					break;

				case DIRECTIVE:
				case COMMENT:
					if( event.getData().length() == 0 )
						break;
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() );
					break;

				case EOF:
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}

		if( text )
			buffer.append( "\"\"\");" );
		buffer.append( "}}}" );

		GroovyTemplate template = new GroovyTemplate( pkg + "." + cls, buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
		log.trace( "Generated Groovy:\n{}", template.getSource() );
		return template;
	}

	// TODO Any other characters?
	static private void writeJavaScriptString( StringBuilder buffer, String s )
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		char c;
		for( int i = 0; i < len; i++ )
			switch( c = chars[ i ] )
			{
				case '"':
				case '\\':
					buffer.append( '\\' ); //$FALL-THROUGH$
				default:
					buffer.append( c );
			}
	}

	static private JavaScriptTemplate toJavaScript( String name, List< ParseEvent > events, List< Directive > directives, List< String > imports )
	{
		StringBuilder buffer = new StringBuilder( 1024 );

		// TODO Should imports be trimmed?
		if( imports != null )
			for( String imprt : imports )
				if( imprt.endsWith( ".*" ) )
					buffer.append( "importPackage(Packages." ).append( imprt.substring( 0, imprt.length() - 2 ) ).append( ");" );
				else
					buffer.append( "importClass(Packages." ).append( imprt ).append( ");" );

		boolean text = false;
		for( ParseEvent event : events )
			switch( event.getEvent() )
			{
				case TEXT:
				case WHITESPACE:
					if( !text )
						buffer.append( "out.write(\"" );
					text = true;
					writeJavaScriptString( buffer, event.getData() );
					break;

				case NEWLINE:
					if( !text )
						buffer.append( "out.write(\"" );
					text = true;
					buffer.append( "\\n\\\n" );
					break;

				case SCRIPT:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( event.getData() ).append( ';' );
					break;

				case EXPRESSION:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( "out.write(" ).append( event.getData() ).append( ");" );
					break;

				case EXPRESSION2:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( "out.writeEncoded(" ).append( event.getData() ).append( ");" );
					break;

				case DIRECTIVE:
				case COMMENT:
					if( event.getData().length() == 0 )
						break;
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( event.getData() );
					break;

				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}

		if( text )
			buffer.append( "\");" );

		JavaScriptTemplate template = new JavaScriptTemplate( name, buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
		log.trace( "Generated JavaScript:\n{}", template.getSource() );
		return template;
	}
}
