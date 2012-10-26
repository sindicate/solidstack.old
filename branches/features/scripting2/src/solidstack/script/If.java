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



public class If extends Expression
{
	private Expression condition;
	private Expression left;
	private Expression right;

	public If( Expression condition, Expression left, Expression right )
	{
		this.condition = condition;
		this.left = left;
		this.right = right;
	}

	@Override
	public Object evaluate( Context context )
	{
		if( Operation.isTrue( Operation.evaluateAndUnwrap( this.condition, context ) ) )
		{
			if( this.left != null )
				return this.left.evaluate( context );
		}
		else
		{
			if( this.right != null )
				return this.right.evaluate( context );
		}
		return null;
	}
}
