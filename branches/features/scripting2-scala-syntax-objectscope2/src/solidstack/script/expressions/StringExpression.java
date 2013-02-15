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
import java.util.ListIterator;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.objects.PString;
import solidstack.script.objects.Util;



public class StringExpression extends LocalizedExpression
{
	private List<Expression> expressions = new ArrayList<Expression>();


	public StringExpression( SourceLocation location )
	{
		super( location );
	}

	public Expression compile()
	{
		ListIterator<Expression> i = this.expressions.listIterator();
		while( i.hasNext() )
			i.set( i.next().compile() );
		return this;
	}

	public PString evaluate( ThreadContext thread )
	{
		List<String> fragments = new ArrayList<String>(); // TODO Or LinkedList?
		List<Object> values = new ArrayList<Object>();
		for( Expression expression : this.expressions )
		{
			Object object = Util.deref( expression.evaluate( thread ) ); // TODO deref() needed here?
			if( expression instanceof StringLiteral )
				fragments.add( (String)object );
			else
			{
				fragments.add( null ); // This is the value indicator
				values.add( object );
			}
		}
		return new PString( fragments.toArray( new String[ fragments.size() ] ), values.toArray() );
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
