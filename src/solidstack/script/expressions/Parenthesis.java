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

package solidstack.script.expressions;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;


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

	public Expression compile()
	{
		if( this.expression == null )
			return null;
		return this.expression.compile(); // Remove Parenthesis from the hierarchy
	}

	public Object evaluate( ThreadContext thread )
	{
		if( this.expression != null )
			return this.expression.evaluate( thread );
		return null;
	}
}
