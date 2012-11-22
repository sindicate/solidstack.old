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

import org.springframework.util.Assert;

import solidstack.io.SourceLocation;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.scopes.AbstractScope.Variable;


public class PreDecr extends Operation
{
	private SourceLocation location;

	public PreDecr( SourceLocation location, String name, Expression right)
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.left );
		Object right = this.right.evaluate( thread );
		if( !( right instanceof Variable ) )
			throw new ScriptException( "Tried to apply " + this.operation + " to a immutable value " + right.getClass().getName() );
		Variable value = (Variable)right;
		Object result = add( value.get(), -1 );
		value.set( result );
		return result;
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
