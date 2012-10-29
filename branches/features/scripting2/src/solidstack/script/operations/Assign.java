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

import java.math.BigDecimal;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.Context.Value;
import solidstack.script.Context.Variable;
import solidstack.script.Expression;
import solidstack.script.FunctionInstance;
import solidstack.script.Operation;
import solidstack.script.ScriptException;
import solidstack.script.SuperString;
import solidstack.script.ThreadContext;
import solidstack.script.TupleValue;


public class Assign extends Operation
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = this.left.evaluate( thread );
		Object right = this.right.evaluate( thread );

		if( right instanceof TupleValue )
		{
			Assert.isInstanceOf( left, TupleValue.class );
			TupleValue leftTuple = (TupleValue)left;
			TupleValue rightTuple = (TupleValue)right;
			int len = leftTuple.size();
			Assert.isTrue( rightTuple.size() == len );
			for( int i = 0; i < len; i++ )
			{
				Object l = leftTuple.get( i );
				Object r = rightTuple.get( i );
				assign( l, r );
			}
		}
		else
		{
			Assert.isFalse( left instanceof TupleValue );
			assign( left, right );
		}

		return right;
	}

	static private void assign( Object var, Object value )
	{
		Assert.notNull( var );
		Assert.notNull( value );
		if( value instanceof Value )
			value = ( (Value)value ).get();
		if( value instanceof BigDecimal || value instanceof String || value instanceof FunctionInstance || value instanceof SuperString || value instanceof Context )
		{
			if( var instanceof Variable )
				( (Variable)var ).set( value );
			else
				throw new ScriptException( "Tried to assign to a immutable value" );
		}
		else
			Assert.fail( "Unexpected " + value.getClass() );
	}
}
