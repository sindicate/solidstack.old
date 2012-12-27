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
import java.util.List;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.objects.FunnyString;
import solidstack.script.objects.Util;



public class StringExpression extends LocalizedExpression
{
	private List<Expression> expressions = new ArrayList<Expression>();


	public StringExpression( SourceLocation location )
	{
		super( location );
	}

	public FunnyString evaluate( ThreadContext thread )
	{
		List<Object> values = new ArrayList<Object>();
		for( Expression expression : this.expressions )
			values.add( Util.deref( expression.evaluate( thread ) ) ); // TODO Or without single()?
		return new FunnyString( values );
	}

	public void append( Expression expression )
	{
		this.expressions.add( expression );
	}

	public int size()
	{
		return this.expressions.size();
	}

	public Expression get( int index )
	{
		return this.expressions.get( index );
	}

	public void writeTo( StringBuilder out )
	{
		for( Expression expression : this.expressions )
		{
			expression.writeTo( out );
			out.append( '+' );
		}
	}
}
