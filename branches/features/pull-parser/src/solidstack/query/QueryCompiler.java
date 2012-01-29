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
import java.util.ArrayList;
import java.util.List;
import solidbase.io.LineReader;
import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser;
import solidstack.template.TemplateCompiler;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;

/**
 * Translates a query template into a Groovy {@link Closure}.
 * 
 * @author René M. de Bloois
 */
public class QueryCompiler extends TemplateCompiler
{
	@Override
	public QueryTemplate translate( String pkg, String cls, LineReader reader )
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
				case EXPRESSION2:
					if( !text )
						buffer.append( "builder.append(\"\"\"" );
					text = true;
					buffer.append( "${" );
					buffer.append( event.getData() );
					buffer.append( '}' );
					break;
				case DIRECTIVE:
					if( directives == null )
						directives = new ArrayList< Directive >();
					directives.addAll( event.getDirectives() );

					for( Directive directive : event.getDirectives() )
						if( ( directive.getName().equals( "template" ) || directive.getName().equals( "query" ) ) && directive.getAttribute().equals( "import" ) )
						{
							if( imports == null )
								imports = new ArrayList< String >();
							imports.add( directive.getValue() );
						}
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
		return new QueryTemplate( buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
	}
}
