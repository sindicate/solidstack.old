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

import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.operations.Function;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.ParameterScope;
import solidstack.script.scopes.Scope;

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

	public Object call( Object... args )
	{
		return call( ThreadContext.get(), args );
	}

	public Object call( ThreadContext thread, Object... pars )
	{
		ParWalker pw = new ParWalker( pars );

		String[] parameters = this.function.getParameters().toArray( new String[ 0 ] ); // TODO Put the array in the function
		int oCount = parameters.length;
		Object[] values = new Object[ oCount ];

		int i = 0;
		int o = 0;
		while( o < oCount )
		{
			String name = parameters[ o ];
			if( name.startsWith( "*" ) )
			{
				parameters[ o ] = name.substring( 1 );

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
				values[ o ] = par;
				o++;
			}
		}
		if( pw.get() != null )
			throw new ScriptException( "Too many parameters given" );

		AbstractScope newScope;
		if( this.function.subScope() )
		{
			Scope scope = new Scope( this.scope );
			for( i = 0; i < oCount; i++ )
				scope.def( parameters[ i ], Script.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
			newScope = scope;
		}
		else if( oCount > 0 )
		{
			ParameterScope parScope = new ParameterScope( this.scope );
			for( i = 0; i < oCount; i++ )
				parScope.defParameter( parameters[ i ], Script.deref( values[ i ] ) ); // TODO If we keep the Link we get output parameters!
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
