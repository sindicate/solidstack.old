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

import java.util.Arrays;
import java.util.List;

public class FunctionInstance implements solidstack.script.java.Function
{
	private Function function;
	private AbstractContext context;

	public FunctionInstance()
	{
	}

	public FunctionInstance( Function function, AbstractContext context )
	{
		this.function = function;
		this.context = context; // FIXME Possibly need to clone the whole context hierarchy (flattened).
	}

	public Object call( Object... args )
	{
		ThreadContext context = ThreadContext.get();
		return call( Arrays.asList( args ), context );
	}

	// FIXME Variable arg
	public Object call( List<Object> pars, ThreadContext thread )
	{
		List<String> parameters = this.function.getParameters();
		int count = parameters.size();
		if( count != pars.size() )
			throw new ScriptException( "Parameter count mismatch" );

		AbstractContext newContext;
		if( this.function.subContext() )
		{
			Context context = new Context( this.context );
			for( int i = 0; i < count; i++ )
			{
				Object value = Operation.unwrap( pars.get( i ) ); // TODO If we keep the Link we get output parameters!
				context.def( parameters.get( i ), value );
			}
			newContext = context;
		}
		else if( count > 0 )
		{
			ParameterContext parContext = new ParameterContext( this.context );
			for( int i = 0; i < count; i++ )
			{
				Object value = Operation.unwrap( pars.get( i ) ); // TODO If we keep the Link we get output parameters!
				parContext.defParameter( parameters.get( i ), value );
			}
			newContext = parContext;
		}
		else
			newContext = this.context;

		AbstractContext old = thread.swapContext( newContext );
		Object result = this.function.getBlock().evaluate( thread );
		thread.swapContext( old );
		return result;
	}
}
