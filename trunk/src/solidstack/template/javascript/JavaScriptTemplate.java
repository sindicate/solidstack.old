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

package solidstack.template.javascript;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.TopLevel;

import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled JavaScript template.
 * 
 * @author René M. de Bloois
 */
public class JavaScriptTemplate extends Template
{
	private Script script;


	/**
	 * Constructor.
	 * 
	 * @param name The name of the template.
	 * @param source The source code of the template. This is the template translated to JavaScript.
	 * @param directives The directives found in the template text.
	 */
	public JavaScriptTemplate( String name, String source, Directive[] directives )
	{
		super( name, source, directives );

		Context cx = Context.enter();
		try
		{
			cx.setOptimizationLevel( -1 ); // Generate only an AST, not bytecode
			this.script = cx.compileString( getSource(), getName(), 1, null ); // TODO Name
		}
		finally
		{
			Context.exit();
		}
	}

	@Override
	public void apply( Map< String, Object > params, EncodingWriter writer )
	{
		Context cx = Context.enter();
		try
		{
			TopLevel scope = new ImporterTopLevel(cx);
			for( Map.Entry< String, Object > param : params.entrySet() )
				scope.put( param.getKey(), scope, param.getValue() );
			scope.put( "out", scope, new JavaScriptConvertingWriter( writer ) );
			cx.executeScriptWithContinuations( this.script, scope );
		}
		finally
		{
			Context.exit();
		}
	}
}
