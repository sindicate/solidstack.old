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

package solidstack.script.scopes;

import java.util.Map;

import solidstack.script.scopes.ValueMap.Entry;


abstract public class AbstractScope implements Scope
{
	abstract public Ref findRef( Symbol symbol );

	public Ref getRef( Symbol symbol )
	{
		Ref v = findRef( symbol );
		if( v == null )
			return new Undefined( symbol );
		return v;
	}

	public Object get( Symbol symbol )
	{
		Ref v = findRef( symbol );
		if( v == null )
			return null;
		return v.get();
	}

	public Object get( String name )
	{
		return get( new TempSymbol( name ) );
	}

	abstract public Variable def( Symbol symbol, Object value );

	abstract public Value val( Symbol symbol, Object value );

	public void set( String name, Object value )
	{
		set( new TempSymbol( name ), value );
	}

	public void set( Symbol symbol, Object value )
	{
		if( setIfExists( symbol, value ) )
			return;
		def( symbol, value );
	}

	public boolean setIfExists( Symbol symbol, Object value )
	{
		Ref v = findRef( symbol );
		if( v == null )
			return false;
		v.set( value );
		return true;
	}

	public void setAll( Map<String, ? extends Object> parameters )
	{
		for( java.util.Map.Entry<String, ? extends Object> entry : parameters.entrySet() )
			set( entry.getKey(), entry.getValue() );
	}

	static public interface Ref
	{
		Symbol getKey();
		boolean isUndefined();
		Object get();
		void set( Object value );
	}

	static public class Value extends Entry implements Ref
	{
		Object value;

		Value( Symbol symbol, Object value )
		{
			super( symbol );

//			Assert.notNull( value );
			this.value = value;
		}

		public Object get()
		{
			return this.value;
		}

		public void set( Object value )
		{
			throw new ScopeException( "'" + getKey() + "' is immutable" );
		}

		public boolean isUndefined()
		{
			return false;
		}
	}

	static public class Variable extends Value
	{
		Variable( Symbol symbol, Object value )
		{
			super( symbol, value );
		}

		@Override
		public void set( Object value )
		{
			this.value = value;
		}
	}

	public class Undefined extends Entry implements Ref
	{
		Undefined( Symbol symbol )
		{
			super( symbol );
		}

		public Object get()
		{
			throw new ScopeException( "'" + getKey() + "' undefined" );
		}

		public void set( Object value )
		{
			def( getKey(), value );
		}

		public boolean isUndefined()
		{
			return true;
		}
	}
}
