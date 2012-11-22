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

import solidstack.lang.Assert;
import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.operations.Function;
import solidstack.script.operations.Spread;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.ParameterScope;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.Symbol;

public class FunctionObject implements solidstack.script.java.Function
{
	private Function function;
	private AbstractScope scope;

	public FunctionObject()
	{
	}

	public FunctionObject( Function function, AbstractScope scope )
	{
		this.function = function;
		this.scope = scope; // FIXME Possibly need to clone the whole scope hierarchy (flattened).
	}

	public Object[] getParameters()
	{
		return this.function.getParameters();
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
			pars = Script.toNamedParameters( pars );
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
				Assert.isTrue( found );
			}
			for( int i = 0; i < oCount; i++ )
				symbols[ i ] = ( (Identifier)parameters[ i ] ).getSymbol();
		}
		else
		{
			ParWalker pw = new ParWalker( pars );

			int i = 0;
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
						throw new ScriptException( "Collecting parameter can only be the last parameter" ); // TODO Also in the middle
				}
				else
				{
					Object par = pw.get();
					if( par == null )
						throw new ScriptException( "Not enough parameters given" );
					symbols[ o ] = ( (Identifier)parameter ).getSymbol();
					values[ o ] = par;
					o++;
				}
			}
			if( pw.get() != null )
				throw new ScriptException( "Too many parameters given" );
		}

		AbstractScope newScope;
		if( this.function.subScope() )
		{
			Scope scope = new Scope( this.scope );
			for( int i = 0; i < oCount; i++ )
			{
				Expression par = parameters[ i ];
				scope.def( symbols[ i ], Script.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
			}
			newScope = scope;
		}
		else if( oCount > 0 )
		{
			ParameterScope parScope = new ParameterScope( this.scope );
			for( int i = 0; i < oCount; i++ )
				parScope.defParameter( symbols[ i ], Script.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newScope = parScope;
		}
		else
			newScope = this.scope;

		AbstractScope old = thread.swapScope( newScope );
		Object result = this.function.getBlock().evaluate( thread );
		thread.swapScope( old );
		return result;
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

		public Object get()
		{
			if( this.tuple != null )
			{
				Object result = this.tuple.get( this.currentInTuple++ ); // TODO deref?
				if( this.currentInTuple >= this.tuple.size() )
					this.tuple = null;
				return result;
			}
			if( this.current >= this.pars.length )
				return null;
			while( true )
			{
				Object result = Script.deref( this.pars[ this.current++ ] );
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
			Object par = get();
			while( par != null )
			{
				result.add( par );
				par = get();
			}
			return result;
		}
	}
}
