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

package solidstack.script;

import java.util.List;

public class FunctionInstance
{
	private Function function;
	private Context context;

	public FunctionInstance()
	{
	}

	public FunctionInstance( Function function, Context context )
	{
		this.function = function;
		this.context = context; // FIXME Possibly need to clone the whole context hierarchy (flattened).
	}

	public Object call( List<?> pars, ThreadContext thread )
	{
		List<String> parameters = this.function.getParameters();
		int count = parameters.size();
		if( count != pars.size() )
			throw new ScriptException( "Parameter count mismatch" );

		Context context = new Context( this.context ); // Subcontext only stores new variables and local (deffed) variables.

		// TODO If we keep the Link we get output parameters!
		for( int i = 0; i < count; i++ )
		{
			Object value = Operation.unwrap( pars.get( i ) ); // TODO Unwrap is also done in the caller
			context.set( parameters.get( i ), value );
		}

		context = thread.swapContext( context );
		Object result = this.function.getBlock().evaluate( thread );
		thread.swapContext( context );

		return result;
	}
}
