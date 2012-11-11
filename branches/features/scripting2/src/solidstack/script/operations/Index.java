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

package solidstack.script.operations;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Operation;


public class Index extends Operation
{
	public Index( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = evaluateAndUnwrap( this.left, thread );
		if( left == null )
			throw new ScriptException( "Cannot index null" );

		// TODO Maybe extend these objects with a index() or at() or getAt() or item()
		if( left instanceof List )
		{
			List<?> list = (List<?>)left;
			Object pars = Operation.unwrap( this.right.evaluate( thread ) );
			Assert.isInstanceOf( pars, BigDecimal.class );
			// TODO Maybe return null when index of out bounds?
			return list.get( ( (BigDecimal)pars ).intValue() );
		}

		if( left instanceof Map )
		{
			Map<?,?> map = (Map<?,?>)left;
			Object pars = Operation.unwrap( this.right.evaluate( thread ) );
			return map.get( pars );
		}

		if( left.getClass().isArray() )
		{
			Object pars = Operation.unwrap( this.right.evaluate( thread ) );
			Assert.isInstanceOf( pars, BigDecimal.class );
			// TODO Maybe return null when index of out bounds?
			return Array.get( left, ( (BigDecimal)pars ).intValue() );
		}

		throw new ScriptException( "Cannot index a " + left.getClass().getName() );
	}
}
