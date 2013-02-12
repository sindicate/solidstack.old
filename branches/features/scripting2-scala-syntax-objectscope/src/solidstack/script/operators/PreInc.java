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

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.scopes.AbstractScope.Variable;


public class PreInc extends Operator
{
	private SourceLocation location;

	public PreInc( SourceLocation location, String name, Expression right)
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.left );
		Object right = this.right.evaluate( thread );
		if( right == null )
			throw new ThrowException( "Can't apply ++ to a null", thread.cloneStack( getLocation() ) );
		if( !( right instanceof Variable ) )
			throw new ThrowException( "Can't apply ++ to a " + right.getClass().getName(), thread.cloneStack( getLocation() ) );
		Variable value = (Variable)right;
		Object result = add( value.get(), 1 );
		value.set( result );
		return result;
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
