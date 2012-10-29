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

import java.util.List;

import solidstack.io.SourceLocation;

public class Function extends LocalizedExpression
{
	private List<String> parameters;
	private Expression block;

	public Function( SourceLocation location, List<String> parameters, Expression block )
	{
		super( location );

		this.parameters = parameters;
		this.block = block;
	}

	public Object evaluate( Context context )
	{
		return new FunctionInstance( this.parameters, this.block, context );
	}
}
