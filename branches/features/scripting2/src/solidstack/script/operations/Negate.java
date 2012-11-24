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
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.Util;


public class Negate extends Operation
{
	private SourceLocation location;

	public Negate( SourceLocation location, String name, Expression right)
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		Assert.isNull( this.left );
		Object right = Util.single( this.right.evaluate( thread ) ); // TODO What about tuples?
		return Operation.negate( right );
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
