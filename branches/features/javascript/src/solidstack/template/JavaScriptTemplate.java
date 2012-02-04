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

import java.io.Writer;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.TopLevel;

import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled template.
 * 
 * @author René M. de Bloois
 */
public class JavaScriptTemplate extends Template
{
	private Script script;


	/**
	 * Constructor.
	 * 
	 * @param source The source code of the template. This is the template translated to the source code of the desired language.
	 * @param directives The directives found in the template text.
	 */
	public JavaScriptTemplate( String source, Directive[] directives )
	{
		super( source, directives );
	}

	/**
	 * Apply this template.
	 * 
	 * @param params The parameters to be applied.
	 * @param writer The result of applying this template is written to this writer.
	 */
	@Override
	public void apply( Map< String, ? > params, Writer writer )
	{
		Context cx = Context.enter();
		try
		{
			TopLevel scope = new ImporterTopLevel(cx);
			for( Map.Entry< String, ? > param : params.entrySet() )
				scope.put( param.getKey(), scope, param.getValue() );
			scope.put( "out", scope, createEncodingWriter( writer ) );
			cx.executeScriptWithContinuations( this.script, scope );
		}
		finally
		{
			Context.exit();
		}
	}

	public void setScript( Script script )
	{
		this.script = script;
	}
}
