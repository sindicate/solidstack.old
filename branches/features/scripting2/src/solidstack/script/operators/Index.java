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

package solidstack.script.operators;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.Null;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.ScopeException;
import solidstack.script.scopes.Symbol;


public class Index extends Operator
{
	public Index( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		try
		{
			Object left = Util.single( this.left.evaluate( thread ) ); // TODO Or index a tuple too?
			if( left == Null.INSTANCE )
				throw new ScriptException( "Cannot index null" );

			Object pars = Util.single( this.right.evaluate( thread ) );

			if( left instanceof Map )
				return new MapItemRef( (Map<?,?>)left, pars );

			Assert.isInstanceOf( pars, Integer.class );

			// TODO Maybe extend these objects with a index() or at() or getAt() or item()
			if( left instanceof List )
				return new ListItemRef( (List<?>)left, (Integer)pars ); // TODO Maybe return null when index of out bounds?

			if( left.getClass().isArray() )
				return Array.get( left, (Integer)pars ); // TODO Maybe return null when index of out bounds?

			throw new ScriptException( "Cannot index a " + left.getClass().getName() );
		}
		catch( ScopeException e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
		}
	}

	static class MapItemRef implements Ref
	{
		private Map map;
		private Object key;

		MapItemRef( Map map, Object key )
		{
			this.map = map;
			this.key = key;
		}

		public Symbol getKey()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isUndefined()
		{
			throw new UnsupportedOperationException();
		}

		public Object get()
		{
			return this.map.get( this.key );
		}

		public void set( Object value )
		{
			this.map.put( this.key, value );
		}
	}

	static class ListItemRef implements Ref
	{
		private List list;
		private int index;

		ListItemRef( List list, int index )
		{
			this.list = list;
			this.index = index;
		}

		public Symbol getKey()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isUndefined()
		{
			throw new UnsupportedOperationException();
		}

		public Object get()
		{
			if( this.index >= this.list.size() )
				return null;
			return this.list.get( this.index );
		}

		public void set( Object value )
		{
			if( this.index >= this.list.size() )
			{
				if( value == null )
					return;
				while( this.index > this.list.size() )
					this.list.add( null );
				this.list.add( value );
				return;
			}
			this.list.set( this.index, value );
		}
	}
}
