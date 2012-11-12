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

import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Operation;


public class LessThan extends Operation
{
	public LessThan( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = Script.deref( this.left.evaluate( thread ) );
		Object right = Script.deref( this.right.evaluate( thread ) );
		return Operation.compare( left, right ) < 0;
	}
}
