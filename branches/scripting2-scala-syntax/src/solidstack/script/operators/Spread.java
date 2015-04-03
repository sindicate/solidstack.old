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

import java.util.List;

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.Tuple;
import solidstack.script.objects.Util;


public class Spread extends Operator
{
	private SourceLocation location;

	public Spread( SourceLocation location, String name, Expression right )
	{
		super( name, null, right );

		this.location = location;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object object = Util.deref( this.right.evaluate( thread ) );
		Assert.isInstanceOf( object, List.class );
		return new Tuple( (List<Object>)object );
	}

	@Override
	public SourceLocation getLocation()
	{
		return this.location;
	}

	public Expression getExpression()
	{
		return this.right;
	}
}
