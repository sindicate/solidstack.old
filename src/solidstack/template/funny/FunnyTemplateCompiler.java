/*--
 * Copyright 2012 Ren� M. de Bloois
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

package solidstack.template.funny;

import solidstack.lang.Assert;
import solidstack.script.Script;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.TemplateCompilerContext;


/**
 * Compiles the given parser events, directives and imports to a {@link FunnyTemplate}.
 *
 * @author Ren� de Bloois
 */
public class FunnyTemplateCompiler
{
	/**
	 * Generates the FunnyScript.
	 *
	 * @param context The compilation context.
	 */
	public void generateScript( TemplateCompilerContext context )
	{
		StringBuilder buffer = new StringBuilder( 1024 );

		boolean text = false;
		for( ParseEvent event : context.getEvents() )
			switch( event.getEvent() )
			{
				case TEXT:
				case NEWLINE:
				case WHITESPACE:
					if( !text )
						buffer.append( "out.write(s\"" );
					text = true;
					writeFunnyString( buffer, event.getData() );
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
					if( !text )
						buffer.append( "out.write(s\"" );
					text = true;
					buffer.append( "${" );
					writeFunnyString( buffer, event.getData() ); // TODO This is not right, why write like this?
					buffer.append( '}' );
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

				case EOF:
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}

		if( text )
			buffer.append( "\");" );

		context.setScript( buffer );
	}

	/**
	 * Compiles the FunnyScript.
	 *
	 * @param context The compilation context.
	 */
	public void compileScript( TemplateCompilerContext context )
	{
		Script script = Script.compile( context.getScript().toString() );
		context.setTemplate( new FunnyTemplate( script ) );
	}

	// TODO Unit test: what if $ or ${ in the string?
	static private void writeFunnyString( StringBuilder buffer, String s )
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		for( int i = 0; i < len; i++ )
		{
			char c;
			switch( c = chars[ i ] )
			{
				case '\b': buffer.append( '\\' ); c = 'b'; break;
				case '\f': buffer.append( '\\' ); c = 'f'; break;
				case '"':
				case '\\':
					buffer.append( '\\' );
			}
			buffer.append( c );
		}
	}
}
