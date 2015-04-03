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


/**
 * An expression that knows its location.
 */
abstract public class LocalizedExpression implements Expression
{
	private SourceLocation location;

	/**
	 * @param location The location of this expression in the source.
	 */
	public LocalizedExpression( SourceLocation location )
	{
		this.location = location;
	}

	public SourceLocation getLocation()
	{
		return this.location;
	}

	public Object evaluateRef( ThreadContext thread )
	{
		return evaluate( thread );
	}
}
