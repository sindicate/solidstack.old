/*--
 * Copyright 2012 Ren� M. de Bloois
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

import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.java.Types;
import solidstack.script.objects.Util;


public class As extends Operator
{
	public As( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = this.left.evaluate( thread );
		Object right = Util.toJava( this.right.evaluate( thread ) );
		Assert.isInstanceOf( right, Class.class );
		return Types.convert( Util.toJava( left ), (Class)right );
	}
}
