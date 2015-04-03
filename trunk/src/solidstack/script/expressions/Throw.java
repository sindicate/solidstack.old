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
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;


public class Throw extends LocalizedExpression
{
	private Expression expression;


	public Throw( SourceLocation location, Expression expression )
	{
		super( location );
		this.expression = expression;
	}

	public Expression compile()
	{
		this.expression = this.expression.compile();
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		if( this.expression == null ) // TODO This could be moved to compile()
			throw new ThrowException( "'throw' expects an expression", thread.cloneStack( getLocation() ) );
		throw new ThrowException( this.expression.evaluate( thread ), thread.cloneStack() );
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "throw " );
		this.expression.writeTo( out );
	}
}
