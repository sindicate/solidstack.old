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

import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.objects.ClassMember;


public class StaticMember extends Operation
{
	public StaticMember( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = Script.single( this.left.evaluate( thread ) );
		Assert.isInstanceOf( Class.class, left );
		Assert.isInstanceOf( Identifier.class, this.right );
		String right = ( (Identifier)this.right ).getSymbol().toString();
		return new ClassMember( (Class<?>)left, right );
	}
}
