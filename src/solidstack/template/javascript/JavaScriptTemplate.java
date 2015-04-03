/*--
 * Copyright 2006 Ren� M. de Bloois
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

import java.io.IOException;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;

import solidstack.io.FatalIOException;
import solidstack.template.ConvertingWriter;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateContext;


/**
 * A compiled JavaScript template.
 *
 * @author Ren� M. de Bloois
 */
public class JavaScriptTemplate extends Template
{
	private Script script;


	/**
	 * @param script The JavaScript script.
	 */
	public JavaScriptTemplate( Script script )
	{
		this.script = script;
	}

	@Override
	public void apply( Object params, EncodingWriter writer )
	{
		Context cx = Context.enter();
		try
		{
			TopLevel topLevel = new ImporterTopLevel( cx );

			ConvertingWriter out = new JavaScriptConvertingWriter( writer );
			topLevel.put( "out", topLevel, out );

			Scriptable scope;
			if( params instanceof Map )
			{
				scope = topLevel;
				for( Map.Entry<String, Object> param : ( (Map<String, Object>)params ).entrySet() )
					scope.put( param.getKey(), scope, Context.javaToJS( param.getValue(), scope ) );
			}
			else
				scope = new NativeJavaObject( topLevel, params, null );

			cx.executeScriptWithContinuations( this.script, scope );
			try
			{
				out.flush();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
		}
		finally
		{
			Context.exit();
		}
	}

	@Override
	public void apply( TemplateContext context )
	{
		apply( context.getParameters(), context.getWriter() );
	}
}
