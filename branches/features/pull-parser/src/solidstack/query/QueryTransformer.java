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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.io.LineReader;
import solidstack.io.PushbackReader;
import solidstack.io.StringLineReader;
import solidstack.template.JSPLikeTemplateParser;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.Util;

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
	static public QueryTemplate compile( LineReader reader, String path, long lastModified )
	{
		LOGGER.info( "compile [" + path + "]" );
		Matcher matcher = pathPattern.matcher( path );
		Assert.isTrue( matcher.matches() );
		path = matcher.group( 1 );
		String name = matcher.group( 2 ).replaceAll( "[\\.-]", "_" );

		String pkg = "solidstack.query.tmp.gsql";
		if( path != null )
			pkg += "." + path.replaceAll( "/", "." );

		String script = translate( pkg, name, reader );
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
		return compile( new StringLineReader( query ), path, lastModified );
	}

	// TODO We should really have some kind og GroovyWriter which can do the escaping
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

	static String translate( String pkg, String cls, LineReader reader )
	{
		JSPLikeTemplateParser parser = new JSPLikeTemplateParser( new PushbackReader( reader ) );
		StringBuilder buffer = new StringBuilder();
		boolean text = false;
		List< String > imports = null;
		loop: while( true )
		{
			ParseEvent event = parser.next3();
			switch( event.getEvent() )
			{
				case TEXT:
				case NEWLINE:
				case WHITESPACE:
					if( !text )
						buffer.append( "builder.append(\"\"\"" );
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
					buffer.append( "builder.append(" );
					buffer.append( event.getData() );
					buffer.append( ");" );
					break;
				case GSTRING:
					if( !text )
						buffer.append( "builder.append(\"\"\"" );
					text = true;
					buffer.append( "${" );
					buffer.append( event.getData() );
					buffer.append( '}' );
					break;
				case DIRECTIVE:
					if( imports == null )
						imports = new ArrayList< String >();
					for( Directive directive : event.getDirectives() )
						imports.add( directive.getValue() );
					//$FALL-THROUGH$
				case COMMENT:
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
		prelude.append( "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );
		buffer.insert( 0, prelude );
		buffer.append( ";return builder.toGString()}}}" ); // Groovy does not understand: "...} return ..." Need extra ; to be sure
		return buffer.toString();
	}

	// For testing purposes
	static String translate( String text )
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

//	static class Writer extends ModalWriter
//	{
//		private String pckg;
//		private String cls;
//		private List< String > imports = new ArrayList< String >();
//
//		public Writer( String pckg, String cls )
//		{
//			this.pckg = pckg;
//			this.cls = cls;
//		}
//
//		@Override
//		protected void directive( String name, String attribute, String value, int lineNumber )
//		{
//			if( !name.equals( "query" ) )
//				throw new ParseException( "Only expecting query directives", lineNumber );
//			if( !attribute.equals( "import" ) )
//				throw new ParseException( "The query directive only allows import attributes", lineNumber );
//			this.imports.add( value );
//		}
//
//		@Override
//		protected void activateMode( Mode mode )
//		{
//			if( this.mode == mode )
//				return;
//
//			// TODO Do this with a matrix switch
//			if( this.mode == Mode.TEXT )
//			{
//				if( mode != Mode.EXPRESSION2 )
//					append( "\"\"\");" );
//			}
//			else if( this.mode == Mode.EXPRESSION )
//				append( ");" );
//			else if( this.mode == Mode.EXPRESSION2 )
//			{
//				append( "}" );
//				if( mode != Mode.TEXT )
//					append( "\"\"\");" );
//			}
//			else if( this.mode == Mode.SCRIPT )
//				append( ';' ); // Groovy does not understand: "...} builder.append(..." Need extra ; when coming from SCRIPT
//			else
//				Assert.fail( "Unknown mode " + this.mode );
//
//			if( mode == Mode.TEXT )
//			{
//				if( this.mode != Mode.EXPRESSION2 )
//					append( "builder.append(\"\"\"" );
//			}
//			else if( mode == Mode.EXPRESSION )
//				append( "builder.append(" );
//			else if( mode == Mode.EXPRESSION2 )
//			{
//				if( this.mode != Mode.TEXT )
//					append( "builder.append(\"\"\"" );
//				append( "${" );
//			}
//			else if( mode != Mode.SCRIPT )
//				Assert.fail( "Unknown mode " + mode );
//
//			this.nextMode = this.mode = mode;
//		}
//
//		@Override
//		protected String getResult()
//		{
//			StringBuilder result = new StringBuilder();
//			result.append( "package " );
//			result.append( this.pckg );
//			result.append( ";" );
//			for( String imprt : this.imports )
//			{
//				result.append( "import " );
//				result.append( imprt );
//				result.append( ';' );
//			}
//			result.append( "class " );
//			result.append( this.cls );
//			result.append( "{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();" );
//			result.append( super.getBuffer() );
//			result.append( ";return builder.toGString()}}}" ); // Groovy does not understand: "...} return ..." Need extra ; to be sure
//			return result.toString();
//		}
//	}
}
