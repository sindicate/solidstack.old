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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.template.GroovyTemplate;
import solidstack.template.TemplateCompiler;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;

/**
 * 
 * 
 * @author René M. de Bloois
 */
public class QueryCompiler extends TemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( QueryCompiler.class );

	@Override
	protected GroovyTemplate toGroovy( String pkg, String cls, List< ParseEvent > events, List< Directive > directives, List< String > imports )
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
					writeString( buffer, event.getData() );
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

		GroovyTemplate template = new GroovyTemplate( cls, buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
		log.trace( "Generated Groovy:\n{}", template.getSource() );
		return template;
	}
}
