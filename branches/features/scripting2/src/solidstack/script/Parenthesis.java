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

package solidstack.script;

import solidstack.io.SourceLocation;


public class Parenthesis extends LocalizedExpression
{
	private Expression expression;


	public Parenthesis( SourceLocation location, Expression expression )
	{
		super( location );
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return this.expression;
	}

	public Object evaluate( ThreadContext thread )
	{
		return this.expression.evaluate( thread );
	}
}
