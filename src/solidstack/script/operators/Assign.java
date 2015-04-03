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

import java.util.List;
import java.util.ListIterator;

import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Expressions;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.Load;
import solidstack.script.expressions.Save;
import solidstack.script.expressions.Var;
import solidstack.script.objects.Tuple;


public class Assign extends Operator
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Expression compile()
	{
		super.compile();

		// exp(args)=value ---> exp.update(value,args)
		if( this.left instanceof Apply )
		{
			// Extract the parts
			Apply apply = (Apply)this.left;
			Expression exp = apply.left;
			Expression value = this.right;
			Expression args = apply.right;

			if( !( args instanceof BuildTuple ) ) // TODO And what if it is?
			{
				// Build new expression from those parts
				Member update = new Member( ".", exp, new Identifier( getLocation(), "update" ) );
				return new Apply( "(", update, new BuildTuple( ",", value, args ) );
			}
		}
		else if( this.left instanceof BuildTuple )
				{
			Save save = new Save( this.right.compile() );

			ListIterator<Expression> i = ( (BuildTuple)this.left ).getExpressions().listIterator();
			int j = 0;
			while( i.hasNext() )
			{
				Expression expression = i.next();
				expression = new Assign( "=", expression, new Load( j++ ) );
				i.set( expression.compile() );
				}

			return new Expressions( save, this.left );
			}

		return this;
		}

	public Object evaluate( ThreadContext thread )
	{
		Object right = this.right.evaluate( thread );

		if( this.left instanceof BuildTuple ) // TODO Move to BuildTuple itself
		{
			if( !( right instanceof Tuple ) )
				throw new UnsupportedOperationException();

			List<Expression> leftTuple = ((BuildTuple)this.left).getExpressions();
				Tuple rightTuple = (Tuple)right;
				int len = leftTuple.size();
				Assert.isTrue( rightTuple.size() == len );
				for( int i = 0; i < len; i++ )
				{
				Expression l = leftTuple.get( i );
				Assert.isTrue( l instanceof Identifier ); // TODO And vars
					Object r = rightTuple.get( i );
				( (Identifier)l ).assign( thread, r );
				}
			return right;
			}

//		if( right instanceof Tuple )
//			throw new ThrowException( "Can't assign tuples to variables", thread.cloneStack( getLocation() ) );

		if( this.left instanceof Identifier )
			return ( (Identifier)this.left ).assign( thread, right );

		if( this.left instanceof Var )
			return ( (Var)this.left ).assign( thread, right );

		if( this.left instanceof Member )
			return ( (Member)this.left ).assign( thread, right );

		throw new ThrowException( "Can't assign to a " + right.getClass().getName(), thread.cloneStack( getLocation() ) );
	}
}
