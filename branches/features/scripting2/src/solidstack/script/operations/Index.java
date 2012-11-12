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
import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Operation;
import solidstack.script.objects.Null;


public class Index extends Operation
{
	public Index( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = Script.single( this.left.evaluate( thread ) ); // TODO Or index a tuple too?
		if( left == Null.INSTANCE )
			throw new ScriptException( "Cannot index null" );

		Object pars = Script.single( this.right.evaluate( thread ) );

		if( left instanceof Map )
			return ( (Map<?,?>)left ).get( pars );

		Assert.isInstanceOf( pars, BigDecimal.class );

		// TODO Maybe extend these objects with a index() or at() or getAt() or item()
		if( left instanceof List )
			return ( (List<?>)left ).get( ( (BigDecimal)pars ).intValue() ); // TODO Maybe return null when index of out bounds?

		if( left.getClass().isArray() )
			return Array.get( left, ( (BigDecimal)pars ).intValue() ); // TODO Maybe return null when index of out bounds?

		throw new ScriptException( "Cannot index a " + left.getClass().getName() );
	}
}
