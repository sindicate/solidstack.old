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
import java.util.ListIterator;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.Tuple;


public class BuildTuple extends Operator
{
	private List<Expression> expressions = new ArrayList<Expression>();

	public BuildTuple( String name, Expression left, Expression right )
	{
		super( name, null, null );
		append( left ).append( right );
	}

	public BuildTuple append( Expression expression )
	{
		this.expressions.add( expression );
		return this;
	}

	public int size()
	{
		return this.expressions.size();
	}

	public List<Expression> getExpressions()
	{
		return this.expressions;
	}

	@Override
	public Expression compile()
	{
		ListIterator<Expression> i = this.expressions.listIterator();
		while( i.hasNext() )
			i.set( i.next().compile() );
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		Tuple result = new Tuple();
		for( Expression expression : this.expressions )
			result.append( expression.evaluate( thread ) );
		return result;
	}

	@Override
	protected Expression getLast()
	{
		return this.expressions.get( this.expressions.size() - 1 );
	}

	@Override
	protected void setLast( Expression expression )
	{
		this.expressions.set( this.expressions.size() - 1, expression );
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.expressions.get( 0 ).getLocation();
	}
}
