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
import solidstack.script.Script;
import solidstack.script.ThreadContext;



public class If extends LocalizedExpression
{
	private Expression condition;
	private Expression left;
	private Expression right;


	public If( SourceLocation location, Expression condition, Expression left, Expression right )
	{
		super( location );

		this.condition = condition;
		this.left = left;
		this.right = right;
	}

	public Expression compile()
	{
		this.condition = this.condition.compile();
		if( this.left != null ) this.left = this.left.compile();
		if( this.right != null ) this.right = this.right.compile();
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		if( Script.isTrue( thread, this.condition ) )
		{
			if( this.left != null )
				return this.left.evaluate( thread );
		}
		else
		{
			if( this.right != null )
				return this.right.evaluate( thread );
		}
		return null;
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "if(" );
		this.condition.writeTo( out );
		out.append( ')' );
		this.left.writeTo( out );
		if( this.right != null )
		{
			out.append( " else " );
			this.right.writeTo( out );
		}
	}
}
