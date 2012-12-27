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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import solidstack.script.ThreadContext;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.Parenthesis;
import solidstack.script.objects.FunctionObject;


public class Function extends Operator
{
	private Expression[] parameters;
	private boolean subScope;

	public Function( String name, Expression args, Expression block )
	{
		super( name, args, block );

		while( args instanceof Parenthesis )
			args = ( (Parenthesis)args ).getExpression();

		List<Expression> parameters = new ArrayList<Expression>();
		if( args instanceof BuildTuple )
		{
			for( Expression par : ( (BuildTuple)args ).getExpressions() )
			{
				Assert.isTrue( par instanceof Spread || par instanceof Identifier );
				parameters.add( par );
			}
		}
		else if( args != null )
		{
			Assert.isTrue( args instanceof Spread || args instanceof Identifier );
			parameters.add( args );
		}
		this.parameters = parameters.toArray( new Expression[ parameters.size() ] );

		if( block instanceof Block )
		{
			this.subScope = true;
			this.right = ( (Block)block ).getExpression();
		}
	}

	public Object evaluate( ThreadContext thread )
	{
		return new FunctionObject( this, thread.getScope() );
	}

	public Expression[] getParameters()
	{
		return this.parameters;
	}

	public Expression getExpression()
	{
		return this.right;
	}

	public boolean subScope()
	{
		return this.subScope;
	}

	@Override
	public void writeTo( StringBuilder out )
	{
		this.left.writeTo( out );
		out.append( this.operator );
		if( this.subScope )
			out.append( '{' );
		else
			out.append( '(' );
		this.right.writeTo( out );
		if( this.subScope )
			out.append( '}' );
		else
			out.append( ')' );
	}
}
