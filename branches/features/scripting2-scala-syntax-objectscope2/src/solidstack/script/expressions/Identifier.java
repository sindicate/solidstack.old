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

import java.util.Map;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.UndefinedPropertyException;
import solidstack.script.scopes.GlobalScope;
import solidstack.script.scopes.UndefinedException;
import funny.Symbol;


public class Identifier extends LocalizedExpression
{
	private Symbol symbol;


	public Identifier( SourceLocation location, String name )
	{
		super( location );

		this.symbol = Symbol.apply( name );
	}

	public Symbol getSymbol()
	{
		return this.symbol;
	}

	public Expression compile()
	{
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		try
		{
			return thread.getScope().get( this.symbol );
		}
		catch( UndefinedException e )
		{
			try
			{
				return GlobalScope.instance.get( this.symbol );
			}
			catch( UndefinedException f )
			{
				throw new UndefinedPropertyException( this.symbol.toString(), thread.cloneStack( getLocation() ) );
			}
		}
	}

	public Object assign( ThreadContext thread, Object value )
	{
		thread.getScope().set( this.symbol, value );
		return value;
	}

	public Object apply( ThreadContext thread, Object[] args )
	{
		try
		{
			return thread.getScope().apply( this.symbol, args );
		}
		catch( UndefinedException e )
		{
			try
			{
				return GlobalScope.instance.apply( this.symbol, args );
			}
			catch( UndefinedException f )
			{
				throw new UndefinedPropertyException( this.symbol.toString(), thread.cloneStack( getLocation() ) );
			}
		}
	}

	public Object apply( ThreadContext thread, Map args )
	{
		try
		{
			return thread.getScope().apply( this.symbol, args );
		}
		catch( UndefinedException e )
		{
			try
			{
				return GlobalScope.instance.apply( this.symbol, args );
			}
			catch( UndefinedException f )
			{
				throw new UndefinedPropertyException( this.symbol.toString(), thread.cloneStack( getLocation() ) );
			}
		}
	}

	public void writeTo( StringBuilder out )
	{
		out.append( this.symbol );
	}

	@Override
	public String toString()
	{
		return this.symbol.toString();
	}
}
