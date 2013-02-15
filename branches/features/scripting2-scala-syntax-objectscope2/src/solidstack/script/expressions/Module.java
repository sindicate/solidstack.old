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
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.CombinedScope;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.GlobalScope;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.UndefinedException;
import funny.Symbol;



public class Module extends LocalizedExpression
{
	private Expression object;
	private Expression expression;

	public Module( SourceLocation location, Expression object, Expression expression )
	{
		super( location );
		this.object = object;
		this.expression = expression;
	}

	public Expression compile()
	{
		this.object = this.object.compile();
		this.expression = this.expression.compile();
		return this;
	}

	// TODO This resembles with() a lot
	public Object evaluate( ThreadContext thread )
	{
		// The name
		Object object = Util.deref( this.object.evaluate( thread ) );
		if( !( object instanceof String ) )
			throw new ThrowException( "Expected a String as module name", thread.cloneStack( getLocation() ) );
		String name = (String)object;

		try
		{
			Scope module = (Scope)GlobalScope.instance.get( Symbol.apply( name ) );
			if( !(Boolean)module.get( Symbol.apply( "initialized" ) ) )
				throw new ThrowException( "Circular module dependency detected", thread.cloneStack( getLocation() ) );
			return module;
		}
		catch( UndefinedException e )
		{
			// Create module scope and define globally
			DefaultScope module = new DefaultScope();
			GlobalScope.instance.set( Symbol.apply( name ), module );
			Ref initializedRef = module.var( Symbol.apply( "initialized" ), false );

			// Continue processing with the module scope
			Scope scope = new CombinedScope( module, thread.getScope() );
			scope = thread.swapScope( scope );
			try
			{
				this.expression.evaluate( thread );
				initializedRef.set( true );
				return module;
			}
			finally
			{
				thread.swapScope( scope );
			}
		}
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "with(" );
		this.object.writeTo( out );
		out.append( ')' );
		this.expression.writeTo( out );
	}
}
