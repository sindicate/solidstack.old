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

import solidstack.io.Resource;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.lang.Assert;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.EVENT;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.funny.FunnyTemplateCompiler;
import solidstack.template.groovy.GroovyTemplateCompiler;
import solidstack.template.javascript.JavaScriptTemplateCompiler;

/**
 * Template compiler.
 *
 * @author René M. de Bloois
 */
// TODO Out.write gstring in queries? Act as JDBC bind parameters or not?
// TODO SQL array parameter in queries
// TODO Include during runtime or compiletime
// TODO Scriptonly: (whole template is just <% fjlkj%>) check no newlines?
public class TemplateCompiler
{
	static final private Pattern CONTENT_TYPE_PATTERN = Pattern.compile( "^[ \\t]*(\\S*)[ \\t]*(?:;[ \\t]*charset[ \\t]*=[ \\t]*(\\S*)[ \\t]*)?$" ); // TODO case sensitive & http://www.iana.org/assignments/media-types/index.html

	static boolean keepSource = false;

	private TemplateLoader loader;


	/**
	 * Constructor.
	 *
	 * @param loader The template loader that created this compiler.
	 */
	public TemplateCompiler( TemplateLoader loader )
	{
		this.loader = loader;
	}

	/**
	 * Compiles a template into a {@link Template}.
	 *
	 * @param resource The {@link Resource} that contains the template.
	 * @param path The path of the template, needed to generate a name for the class in memory.
	 * @return A {@link Template}.
	 */
	// TODO CRLF (Windows) and CR (Macintosh) are always translated to LF, what if the result should have CRLF or CR?
	// TODO Test empty package
	public Template compile( Resource resource, String path )
	{
		Loggers.compiler.info( "Compiling [{}] from [{}]", path, resource );

		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setPath( path );
		context.setResource( resource );

		compile( context );

		return context.getTemplate();
	}

	/**
	 * Compiles a template into a {@link Template}.
	 *
	 * @param reader The {@link SourceReader} that contains the template.
	 * @param path The path of the template, needed to generate a name for the class in memory.
	 * @return A {@link Template}.
	 */
	public Template compile( SourceReader reader, String path )
	{
		Loggers.compiler.info( "Compiling [{}] from [{}]", path, reader.getResource() );

		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setPath( path );
		context.setReader( reader );

		compile( context );

		return context.getTemplate();
	}

	/**
	 * Compiles a template into a {@link Template}.
	 *
	 * @param context The compilation context.
	 */
	public void compile( TemplateCompilerContext context )
	{
		createReader( context );
		try
		{
			parse( context );
			consolidateWhitespace( context );
			collectDirectives( context );
			processDirectives( context );

			String lang = context.getLanguage();
			if( lang == null )
				if( this.loader != null )
				{
					lang = this.loader.getDefaultLanguage();
					if( lang == null )
						throw new TemplateException( "Template has no \"language\" directive, and no defaultLanguage configured in the TemplateLoader" );
				}
				else
					throw new TemplateException( "Template has no \"language\" directive" );

			if( lang.equals( "funny" ) )
			{
				FunnyTemplateCompiler compiler = new FunnyTemplateCompiler();
				compiler.generateScript( context );
				Loggers.compiler.trace( "Generated FunnyScript:\n{}", context.getScript() );
				compiler.compileScript( context );
			}
			else if( lang.equals( "javascript" ) )
			{
				JavaScriptTemplateCompiler compiler = new JavaScriptTemplateCompiler();
				compiler.generateScript( context );
				Loggers.compiler.trace( "Generated JavaScript:\n{}", context.getScript() );
				compiler.compileScript( context );
			}
			else if( lang.equals( "groovy" ) )
			{
				GroovyTemplateCompiler compiler = new GroovyTemplateCompiler();
				compiler.generateScript( context );
				Loggers.compiler.trace( "Generated Groovy:\n{}", context.getScript() );
				compiler.compileScript( context );
			}
			else
				throw new TemplateException( "Unsupported scripting language: " + lang );

			configureTemplate( context );
		}
		finally
		{
			closeReader( context );
		}
	}

	/**
	 * Creates a reader.
	 *
	 * @param context The compilation context.
	 */
	protected void createReader( TemplateCompilerContext context )
	{
		if( context.getReader() != null ) // TODO Why is this?
			return;

		try
		{
			context.setReader( SourceReaders.forResource( context.getResource(), EncodingDetector.INSTANCE ) );
		}
		catch( FileNotFoundException e )
		{
			throw new TemplateNotFoundException( context.getResource().getNormalized() + " not found" );
		}
	}

	protected void closeReader( TemplateCompilerContext context )
	{
		context.getReader().close();
	}

	/**
	 * Parses the source.
	 *
	 * @param context The compilation context.
	 */
	protected void parse( TemplateCompilerContext context )
	{
		// Parse and collect directives
		JSPLikeTemplateParser parser = new JSPLikeTemplateParser( context.getReader() );
		List< ParseEvent > events = new ArrayList< ParseEvent >();
		ParseEvent event = parser.next();
		while( event.getEvent() != EVENT.EOF )
		{
			events.add( event );
			event = parser.next();
		}
		context.setEvents( events );
	}

	protected void consolidateWhitespace( TemplateCompilerContext context )
	{
		List<ParseEvent> events = context.getEvents();
		List<ParseEvent> result = new ArrayList<ParseEvent>();
		int len = events.size();
		int start = 0;
		boolean hasText = false;
		boolean hasScript = false;
		for( int i = 0; i < len; i++ )
		{
			ParseEvent event = events.get( i );
			switch( event.getEvent() )
			{
				case EXPRESSION:
				case EXPRESSION2:
				case TEXT:
					hasText = true;
					break;
				case DIRECTIVE:
				case COMMENT:
				case SCRIPT:
					if( event.getData().indexOf( '\n' ) >= 0 )
					{
						consolidate( events, start, i - 1, result, hasText, true );
						start = i;
						hasText = false;
					}
					hasScript = true;
					break;
				case WHITESPACE:
					break;
				case NEWLINE:
					consolidate( events, start, i, result, hasText, hasScript );
					start = i + 1;
					hasText = hasScript = false;
					break;
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}
		}
		if( start < len )
			consolidate( events, start, len - 1, result, hasText, hasScript );
		context.setEvents( result );
	}

	private static void consolidate( List<ParseEvent> source, int start, int end, List<ParseEvent> result, boolean hasText, boolean hasScript )
	{
		if( hasText || !hasScript )
		{
			for( int i = start; i <= end; i++ )
				result.add( source.get( i ) );
			return;
		}

		ParseEvent last = null;
		for( int i = start; i <= end; i++ )
		{
			ParseEvent event = source.get( i );
			switch( event.getEvent() )
			{
				case WHITESPACE:
					break;
				case DIRECTIVE:
				case COMMENT:
				case SCRIPT:
					result.add( event );
					last = event;
					break;
				case NEWLINE:
					Assert.isTrue( i == end );
					last.setData( last.getData() + event.getData() );
					break;
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}
		}
	}

	/**
	 * Collects the directives.
	 *
	 * @param context The compilation context.
	 */
	protected void collectDirectives( TemplateCompilerContext context )
	{
		List<Directive> directives = new ArrayList<Directive>();
		for( ParseEvent event : context.getEvents() )
			if( event.getEvent() == EVENT.DIRECTIVE )
				directives.addAll( event.getDirectives() );
		context.setDirectives( directives );
	}

	/**
	 * Processes the directives.
	 *
	 * @param context The compilation context.
	 */
	protected void processDirectives( TemplateCompilerContext context )
	{
		List< String > imports = new ArrayList< String >();
		String lang = null;
		String contentType = null;
		if( context.getDirectives() != null )
			for( Directive directive : context.getDirectives() )
				if( directive.getName().equals( "template" ) )
					if( directive.getAttribute().equals( "import" ) )
						imports.add( directive.getValue() );
					else if( directive.getAttribute().equals( "language" ) )
						lang = directive.getValue();
					else if( directive.getAttribute().equals( "contentType" ) )
						contentType = directive.getValue();
		context.setImports( imports );
		context.setLanguage( lang );
		if( contentType != null )
		{
			Matcher matcher = CONTENT_TYPE_PATTERN.matcher( contentType );
			Assert.isTrue( matcher.matches(), "Couldn't interpret contentType " + contentType );
			context.setContentType( matcher.group( 1 ) );
			context.setCharSet( matcher.group( 2 ) );
		}
	}

	/**
	 * Configures the template.
	 *
	 * @param context The compilation context.
	 */
	protected void configureTemplate( TemplateCompilerContext context )
	{
		Template template = context.getTemplate();
		template.setPath( context.getPath() );
		template.setDirectives( context.getDirectivesArray() );
		template.setContentType( context.getContentType() );
		template.setCharSet( context.getCharSet() );
	}
}
