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

import org.springframework.util.Assert;

import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.scopes.AbstractScope.Variable;


public class PostDecr extends Operator
{
	public PostDecr( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.right );
		Object left = this.left.evaluate( thread );
		if( left == null )
			throw new ThrowException( "Can't apply -- to a null", thread.cloneStack( getLocation() ) );
		if( !( left instanceof Variable ) )
			throw new ThrowException( "Can't apply -- to a " + left.getClass().getName(), thread.cloneStack( getLocation() ) );
		Variable value = (Variable)left;
		Object result = value.get();
		value.set( add( result, -1 ) );
		return result;
	}
}
