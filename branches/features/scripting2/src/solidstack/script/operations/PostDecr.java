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

import java.math.BigDecimal;

import org.springframework.util.Assert;

import solidstack.script.Context.Variable;
import solidstack.script.Expression;
import solidstack.script.Operation;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;


public class PostDecr extends Operation
{
	public PostDecr( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.right );
		Object left = this.left.evaluate( thread );
		if( !( left instanceof Variable ) )
			throw new ScriptException( "Tried to apply " + this.operation + " to a immutable value " + left.getClass().getName() );
		Variable value = (Variable)left;
		Object result = value.get();
		value.set( add( result, new BigDecimal( -1 ) ) );
		return result;
	}
}
