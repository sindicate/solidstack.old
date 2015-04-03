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

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;


public class Not extends Operator
{
	private SourceLocation location;

	public Not( SourceLocation location, String name, Expression right)
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.left );
		Object right = this.right.evaluate( thread );
		return !Script.isTrue( right );
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
