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

package solidstack.script.objects;

import java.util.List;

import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Function;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.ParameterScope;

public class FunctionObject implements solidstack.script.java.Function
{
	private Function function;
	private AbstractScope scope;

	public FunctionObject()
	{
	}

	public FunctionObject( Function function, AbstractScope scope )
	{
		this.function = function;
		this.scope = scope; // FIXME Possibly need to clone the whole context hierarchy (flattened).
	}

	public Object call( Object... args )
	{
		return call( ThreadContext.get(), args );
	}

	public Object call( ThreadContext thread, Object... pars )
	{
		List<String> parameters = this.function.getParameters();
		int count = parameters.size();
		if( count != pars.length )
			throw new ScriptException( "Parameter count mismatch" );

		AbstractScope newContext;
		if( this.function.subScope() )
		{
			Scope context = new Scope( this.scope );
			for( int i = 0; i < count; i++ )
				context.def( parameters.get( i ), pars[ i ] ); // TODO If we keep the Link we get output parameters!
			newContext = context;
		}
		else if( count > 0 )
		{
			ParameterScope parContext = new ParameterScope( this.scope );
			for( int i = 0; i < count; i++ )
				parContext.defParameter( parameters.get( i ), Script.deref( pars[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newContext = parContext;
		}
		else
			newContext = this.scope;

		AbstractScope old = thread.swapScope( newContext );
		Object result = this.function.getBlock().evaluate( thread );
		thread.swapScope( old );
		return result;
	}
}
