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



public class Parenthesis extends Expression
{
	private Expression expression;

	public Parenthesis( Expression expression )
	{
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return this.expression;
	}

	@Override
	public Object evaluate( Context context )
	{
		return this.expression.evaluate( context );
	}

	@Override
	public Object assign( Context context, Object value )
	{
		return this.expression.assign( context, value );
	}
}
