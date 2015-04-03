/*--
 * Copyright 2012 René M. de Bloois
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

import java.io.IOException;
import java.util.Map;

import solidstack.io.FatalIOException;
import solidstack.script.Script;
import solidstack.script.scopes.CombinedScope;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.MapScope;
import solidstack.script.scopes.ObjectScope;
import solidstack.script.scopes.Scope;
import solidstack.template.ConvertingWriter;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;


/**
 * A compiled FunnyScript template.
 *
 * @author René M. de Bloois
 */
public class FunnyTemplate extends Template
{
	static public final Symbol OUT = Symbol.forString( "out" );

	private Script script;

	public FunnyTemplate( Script script )
	{
		this.script = script;
	}

	@Override
	public void apply( Object params, EncodingWriter writer )
	{
		FunnyTemplateHelper helper = new FunnyTemplateHelper( this, params, writer );

		// TODO Is this what we want?
		Scope scope;
		if( params instanceof Map<?, ?> )
			scope = new MapScope( (Map<Object, Object>)params );
		else
			scope = new ObjectScope( params ); // TODO Test

		scope = new CombinedScope( scope, new ObjectScope( helper ) );

		scope = new DefaultScope( scope );

		/* TODO Do we need the default scope?
		for( Entry<String, Object> entry : params.entrySet() )
			scope.var( Symbol.apply( entry.getKey() ), entry.getValue() );
		*/

		// TODO What about 'this'?
		ConvertingWriter out = new FunnyConvertingWriter( writer );
		scope.var( OUT, out );

		this.script.eval( scope );

		try
		{
			out.flush();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
