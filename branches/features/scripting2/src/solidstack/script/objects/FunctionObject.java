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
import solidstack.script.operations.Function;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.ParameterScope;
import solidstack.script.scopes.Scope;

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
		this.scope = scope; // FIXME Possibly need to clone the whole scope hierarchy (flattened).
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

		AbstractScope newScope;
		if( this.function.subScope() )
		{
			Scope scope = new Scope( this.scope );
			for( int i = 0; i < count; i++ )
				scope.def( parameters.get( i ), pars[ i ] ); // TODO If we keep the Link we get output parameters!
			newScope = scope;
		}
		else if( count > 0 )
		{
			ParameterScope parScope = new ParameterScope( this.scope );
			for( int i = 0; i < count; i++ )
				parScope.defParameter( parameters.get( i ), Script.deref( pars[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newScope = parScope;
		}
		else
			newScope = this.scope;

		AbstractScope old = thread.swapScope( newScope );
		Object result = this.function.getBlock().evaluate( thread );
		thread.swapScope( old );
		return result;
	}
}
