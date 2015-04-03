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

package solidstack.script.operators;

import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Tuple;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope.Ref;


public class Assign extends Operator
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		if( this.left instanceof Apply )
		{
			// TODO This is ugly
			Apply apply = (Apply)this.left;
			Expression object = apply.left;
			if( !( object instanceof Identifier && ( (Identifier)object ).getSymbol().toString().equals( "var" ) ) )
			{
				Expression pars = apply.right;
				if( !( pars instanceof BuildTuple ) ) // TODO And what if it is?
				{
					Member update = new Member( ".", object, new Identifier( getLocation(), "update" ) );
					BuildTuple par = new BuildTuple( ",", pars, this.right );
					return new Apply( "(", update, par ).evaluate( thread );
				}
			}
		}

		Object left = this.left.evaluateRef( thread );
		Object right = Util.deref( this.right.evaluate( thread ) );

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
					assign( l, r, thread );
				}
			}
			else
				throw new UnsupportedOperationException();
		}
		else
			assign( left, right, thread );

		return right; // TODO Or should it be left? Or should we do assignment like this 1 => a?
	}

	private void assign( Object var, Object value, ThreadContext thread )
	{
		Assert.notNull( var );
		value = Util.finalize( value );
		if( value instanceof Tuple )
			throw new ThrowException( "Can't assign tuples to variables", thread.cloneStack( getLocation() ) );
		if( value instanceof FunctionObject )
			( (FunctionObject)value ).setAssigned();
		if( !( var instanceof Ref ) )
			throw new ThrowException( "Can't assign to a " + var.getClass().getName(), thread.cloneStack( getLocation() ) );
		( (Ref)var ).set( value );
	}
}
