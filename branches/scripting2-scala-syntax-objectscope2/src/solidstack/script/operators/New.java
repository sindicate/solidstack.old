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
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;


public class New extends Operator
{
	private SourceLocation location;

	public New( SourceLocation location, String name, Expression right )
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		throw new UnsupportedOperationException();
	}

	public Object evaluateForApply( ThreadContext thread )
	{
		return this.right.evaluate( thread );
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
