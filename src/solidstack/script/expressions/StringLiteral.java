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



public class StringLiteral extends LocalizedExpression
{
	private String value;


	public StringLiteral( SourceLocation location, String value )
	{
		super( location );

		this.value = value;
	}

	public String getString()
	{
		return this.value;
	}

	public Expression compile()
	{
		return this;
	}

	public String evaluate( ThreadContext thread )
	{
		return this.value;
	}

	public void writeTo( StringBuilder out )
	{
		// TODO Escape the string literal
		out.append( '"' ).append( this.value ).append( '"' );
	}
}
