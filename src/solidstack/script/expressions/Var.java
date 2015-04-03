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


public class Var extends LocalizedExpression
{
	private Identifier identifier;


	public Var( SourceLocation location, Identifier identifier )
	{
		super( location );
		this.identifier = identifier;
	}

	public Expression compile()
	{
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		thread.getScope().var( this.identifier.getSymbol(), null );
		return null;
	}

	public Object assign( ThreadContext thread, Object value )
	{
		thread.getScope().var( this.identifier.getSymbol(), value );
		return value;
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "var " );
		this.identifier.writeTo( out );
	}
}
