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

package solidstack.script.operations;

import java.util.ArrayList;
import java.util.List;

import solidstack.io.SourceException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.Parenthesis;
import solidstack.script.objects.FunctionObject;


public class Function extends Operation
{
	private List<String> parameters;
	private boolean subScope;

	public Function( String name, Expression args, Expression block )
	{
		super( name, args, block );

		while( args instanceof Parenthesis )
			args = ( (Parenthesis)args ).getExpression();

		this.parameters = new ArrayList<String>();
		if( args instanceof BuildTuple )
		{
			for( Expression par : ( (BuildTuple)args ).getExpressions() )
			{
				if( !( par instanceof Identifier ) )
					throw new SourceException( "Expected an identifier", par.getLocation() );
				this.parameters.add( ( (Identifier)par ).getName() );
			}
		}
		else if( args != null )
		{
			if( !( args instanceof Identifier ) )
				throw new SourceException( "Expected an identifier", args.getLocation() );
			this.parameters.add( ( (Identifier)args ).getName() );
		}

		if( block instanceof Block )
		{
			this.subScope = true;
			this.right = ( (Block)block ).getExpression();
		}
	}

	public Object evaluate( ThreadContext thread )
	{
		return new FunctionObject( this, thread.getScope() );
	}

	public List<String> getParameters()
	{
		return this.parameters;
	}

	public Expression getBlock()
	{
		return this.right;
	}

	public boolean subScope()
	{
		return this.subScope;
	}
}
