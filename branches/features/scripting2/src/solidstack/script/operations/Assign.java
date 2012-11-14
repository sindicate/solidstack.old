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

import solidstack.lang.Assert;
import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.Tuple;
import solidstack.script.scopes.AbstractScope.Variable;


public class Assign extends Operation
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = this.left.evaluate( thread );
		Object right = Script.deref( this.right.evaluate( thread ) );

		if( left instanceof Tuple )
		{
			if( right instanceof Tuple )
			{
				Tuple leftTuple = (Tuple)left;
				Tuple rightTuple = (Tuple)right;
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
				throw new UnsupportedOperationException();
		}
		else
			assign( left, right );

		return right; // TODO Or should it be left? Or should we do assignment like this 1 => a?
	}

	static private void assign( Object var, Object value )
	{
		Assert.notNull( var );
		Assert.notNull( value );
		if( var instanceof Variable )
			( (Variable)var ).set( Script.deref( value ) );
		else
			throw new ScriptException( "Tried to assign to a immutable value" );
	}
}
