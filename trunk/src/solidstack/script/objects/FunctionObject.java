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

import solidstack.lang.Assert;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.operators.Assign;
import solidstack.script.operators.Function;
import solidstack.script.operators.Spread;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.ParameterScope;
import solidstack.script.scopes.Scope;
import funny.Symbol;

public class FunctionObject implements solidstack.script.java.Function
{
	private Function function;
	private Scope scope;
	private boolean assigned; // FIXME Remove this, it does not work correctly. For example, with functions returning functions.

	public FunctionObject()
	{
	}

	public FunctionObject( Function function, Scope scope )
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

	public Object call( ThreadContext thread, Object... pars )
	{
		Expression[] parameters = this.function.getParameters();
		int oCount = parameters.length;
		Symbol[] symbols = new Symbol[ oCount ];
		Object[] values = new Object[ oCount ];

		if( pars.length > 0 && pars[ 0 ] instanceof Labeled )
		{
			pars = Util.toNamedParameters( pars, thread );
			int count = pars.length;
			int index = 0;
			while( index < count )
			{
				boolean found = false;
				Symbol label = (Symbol)pars[ index++ ];
				for( int i = 0; i < oCount; i++ )
					if( ( (Identifier)parameters[ i ] ).getSymbol().equals( label ) )
					{
						values[ i ] = pars[ index++ ];
						found = true;
						break;
					}
				if( !found )
					throw new ThrowException( "Parameter '" + label + "' undefined", thread.cloneStack() );
			}
			for( int i = 0; i < oCount; i++ )
				symbols[ i ] = ( (Identifier)parameters[ i ] ).getSymbol();
		}
		else
		{
			ParWalker pw = new ParWalker( pars );

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
			else if( parameter instanceof Assign )
			{
				Assign assign = (Assign)parameter;
				Assert.isInstanceOf( assign.getLeft(), Identifier.class );
				Object par;
				if( pw.hasNext() )
					par = pw.get();
				else
				{
					Scope old = thread.swapScope( this.scope );
					try
					{
						par = assign.getRight().evaluate( thread );
					}
					finally
					{
						thread.swapScope( old );
					}
				}
				symbols[ o ] = ( (Identifier)assign.getLeft() ).getSymbol();
				values[ o ] = par;
				o++;
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
		}

	public Object call( ThreadContext thread, Map<Symbol, Object> args )
	{
		Expression[] parameters = this.function.getParameters();
		int oCount = parameters.length;
		Symbol[] symbols = new Symbol[ oCount ];
		Object[] values = new Object[ oCount ];

		for( int i = 0; i < oCount; i++ )
		{
			Expression parameter = parameters[ i ];
			if( parameter instanceof Assign )
			{
				Assign assign = (Assign)parameter;
				Assert.isInstanceOf( assign.getLeft(), Identifier.class );
				Symbol symbol = ( (Identifier)assign.getLeft() ).getSymbol();
				symbols[ i ] = symbol;
				if( args.containsKey( symbol ) )
					values[ i ] = args.remove( symbol );
				else
				{
					Scope old = thread.swapScope( this.scope );
					try
					{
						values[ i ] = assign.getRight().evaluate( thread );
					}
					finally
					{
						thread.swapScope( old );
					}
				}
			}
			else
			{
				Symbol symbol = ( (Identifier)parameters[ i ] ).getSymbol();
				symbols[ i ] = symbol;
				if( args.containsKey( symbol ) )
					values[ i ] = args.remove( symbol );
				else
					throw new ThrowException( "No value specified for parameter '" + symbol + "'", thread.cloneStack() );
			}
		}

		if( !args.isEmpty() )
		{
			Symbol symbol = args.keySet().iterator().next();
			throw new ThrowException( "Parameter '" + symbol + "' undefined", thread.cloneStack() );
		}

		return call( thread, symbols, values );
	}

	private Object call( ThreadContext thread, Symbol[] symbols, Object[] values )
	{
		int count = values.length;

		Scope newScope;
		if( this.function.subScope() )
		{
			DefaultScope scope = new DefaultScope( this.scope );
			for( int i = 0; i < oCount; i++ )
				scope.var( symbols[ i ], values[ i ] ); // TODO If we keep the Link we get output parameters!
			newScope = scope;
		}
		else if( oCount > 0 )
		{
			ParameterScope parScope = new ParameterScope( this.scope );
			for( int i = 0; i < oCount; i++ )
				parScope.defParameter( symbols[ i ], values[ i ] ); // TODO If we keep the Link we get output parameters!
			newScope = parScope;
		}
		else
			newScope = this.scope;

		Scope old = thread.swapScope( newScope );
		try
		{
			if( this.function.getExpression() == null )
				return null;
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
				Object result = this.pars[ this.current++ ];
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
