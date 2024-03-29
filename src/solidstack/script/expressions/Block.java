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
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.Scope;


public class Block extends LocalizedExpression // TODO Is this localized needed?
{
	private Expression expression;


	public Block( SourceLocation location, Expression expression )
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
		this.expression = this.expression.compile();
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		Scope old = thread.swapScope( new DefaultScope( thread.getScope() ) );
		try
		{
			return this.expression.evaluate( thread );
		}
		finally
		{
			thread.swapScope( old );
		}
	}

	public void writeTo( StringBuilder out )
	{
		out.append( '{' );
		this.expression.writeTo( out );
		out.append( '}' );
	}
}
