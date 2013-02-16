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

package solidstack.script.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.ThreadContext;

public class Expressions implements Expression
{
	private List<Expression> expressions = new ArrayList<Expression>();

	public Expressions( Expression... expressions )
	{
		Collections.addAll( this.expressions, expressions );
	}

	public Expressions( Expression expression )
	{
		if( expression != null )
			append( expression );
	}

	public List<Expression> getExpressions()
	{
		return this.expressions;
	}

	public Expression compile()
	{
		List<Expression> result = new ArrayList<Expression>();

		for( Expression expression : this.expressions )
			if( expression != null )
			{
				expression = expression.compile();
				if( expression != null )
				{
					if( expression instanceof Expressions )
						result.addAll( ( (Expressions)expression ).getExpressions() );
					else
						result.add( expression );
				}
			}

		if( result.isEmpty() ) return null;
		if( result.size() == 1 ) return result.get( 0 );
		this.expressions = result;

		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object result = null;
		for( Expression e : this.expressions )
			if( e != null )
				result = e.evaluate( thread );
		return result;
	}

	public Expressions append( Expression expression )
	{
		this.expressions.add( expression );
		return this;
	}

	public int size()
	{
		return this.expressions.size();
	}

	public Expression get( int index )
	{
		return this.expressions.get( index );
	}

	public Expression remove( int index )
	{
		return this.expressions.remove( 0 );
	}

	public SourceLocation getLocation()
	{
		Assert.isTrue( !this.expressions.isEmpty() );
		return this.expressions.get( 0 ).getLocation();
	}

	public void writeTo( StringBuilder out )
	{
		for( Expression expression : this.expressions )
		{
			expression.writeTo( out );
			out.append( ';' );
		}
	}
}
