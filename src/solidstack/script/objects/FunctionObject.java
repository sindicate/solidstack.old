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

package solidstack.script.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import funny.Symbol;

import solidstack.lang.Assert;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.operators.Function;
import solidstack.script.operators.Spread;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.ParameterScope;
import solidstack.script.scopes.Scope;

public class FunctionObject implements solidstack.script.java.Function
{
	private Function function;
	private AbstractScope scope;
	private boolean assigned; // FIXME Remove this, it does not work correctly. For example, with functions returning functions.

	public FunctionObject()
	{
	}

	public FunctionObject( Function function, AbstractScope scope )
	{
		this.function = function;
		this.scope = scope; // FIXME Possibly need to clone the whole scope hierarchy (flattened).
	}

	public void setAssigned()
	{
		this.assigned = true;
	}

	public boolean isAssigned()
	{
		return this.assigned;
	}

	public Object[] getParameters()
	{
		return this.function.getParameters();
	}

	public Function getFunction()
	{
		return this.function;
	}

	public Object call( Object... args )
	{
		return call( ThreadContext.get(), args );
	}

	public Object call( ThreadContext thread, Object... args )
	{
		Expression[] parameters = this.function.getParameters();
		int oCount = parameters.length;
		Symbol[] symbols = new Symbol[ oCount ];
		Object[] values = new Object[ oCount ];

		ParWalker pw = new ParWalker( args );

		int o = 0;
		while( o < oCount )
		{
			Expression parameter = parameters[ o ];
			if( parameter instanceof Spread )
			{
				parameter = ( (Spread)parameter ).getExpression();
				symbols[ o ] = ( (Identifier)parameter ).getSymbol();
				values[ o ] = pw.rest();
				o++;
				if( o < oCount )
					throw new ThrowException( "Collecting parameter must be the last parameter", thread.cloneStack() ); // TODO Also in the middle
			}
			else
			{
				if( !pw.hasNext() )
					throw new ThrowException( "Not enough parameters", thread.cloneStack() );
				Object par = pw.get();
				symbols[ o ] = ( (Identifier)parameter ).getSymbol();
				values[ o ] = par;
				o++;
			}
		}
		if( pw.hasNext() )
			throw new ThrowException( "Too many parameters", thread.cloneStack() );

		return call( thread, symbols, values );
	}

	public Object call( ThreadContext thread, Map<Symbol, Object> args )
	{
		Expression[] parameters = this.function.getParameters();
		int oCount = parameters.length;
		Symbol[] symbols = new Symbol[ oCount ];
		Object[] values = new Object[ oCount ];

		for( Entry<Symbol, Object> entry : args.entrySet() )
		{
			boolean found = false;
			Symbol label = entry.getKey();
			for( int i = 0; i < oCount; i++ )
				if( ( (Identifier)parameters[ i ] ).getSymbol().equals( label ) )
				{
					values[ i ] = entry.getValue();
					found = true;
					break;
				}
			if( !found )
				throw new ThrowException( "Parameter '" + label + "' undefined", thread.cloneStack() );
		}
		for( int i = 0; i < oCount; i++ )
			symbols[ i ] = ( (Identifier)parameters[ i ] ).getSymbol();

		return call( thread, symbols, values );
	}

	private Object call( ThreadContext thread, Symbol[] symbols, Object[] values )
	{
		int count = values.length;

		AbstractScope newScope;
		if( this.function.subScope() )
		{
			Scope scope = new Scope( this.scope );
			for( int i = 0; i < count; i++ )
				scope.def( symbols[ i ], Util.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newScope = scope;
		}
		else if( count > 0 )
		{
			ParameterScope parScope = new ParameterScope( this.scope );
			for( int i = 0; i < count; i++ )
				parScope.defParameter( symbols[ i ], Util.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newScope = parScope;
		}
		else
			newScope = this.scope;

		AbstractScope old = thread.swapScope( newScope );
		try
		{
			return this.function.getExpression().evaluate( thread );
		}
		catch( Returning e )
		{
			if( this.assigned )
				return e.getValue();
			throw e;
		}
		finally
		{
			thread.swapScope( old );
		}
	}

	static public class ParWalker
	{
		private Object[] pars;
		int current;
		private Tuple tuple;
		int currentInTuple;

		public ParWalker( Object... pars )
		{
			this.pars = pars;
		}

		public boolean hasNext()
		{
			return this.tuple != null || this.current < this.pars.length;
		}

		public Object get()
		{
			if( this.tuple != null )
			{
				Object result = this.tuple.get( this.currentInTuple++ ); // TODO deref?
				if( this.currentInTuple >= this.tuple.size() )
					this.tuple = null;
				return result;
			}
			Assert.isTrue( this.current < this.pars.length );
			while( true )
			{
				Object result = Util.deref( this.pars[ this.current++ ] );
				if( !( result instanceof Tuple ) )
					return result;
				Tuple t = (Tuple)result;
				if( t.size() > 1 )
				{
					this.tuple = t;
					this.currentInTuple = 1;
					return t.get( 0 );
				}
				if( t.size() > 0 )
					return t.get( 0 );
			}
		}

		public List<Object> rest()
		{
			List<Object> result = new ArrayList<Object>();
			while( hasNext() )
				result.add( get() );
			return result;
		}
	}
}
