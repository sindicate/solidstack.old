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

package solidstack.script.expressions;

import solidstack.io.SourceLocation;
import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.objects.Null;



public class While extends LocalizedExpression
{
	private Expression condition;
	private Expression left;

	public While( SourceLocation location, Expression condition, Expression left )
	{
		super( location );
		this.condition = condition;
		this.left = left;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object result = Null.INSTANCE;
		while( Script.isTrue( this.condition.evaluate( thread ) ) )
			result = this.left.evaluate( thread );
		return result;
	}
}
