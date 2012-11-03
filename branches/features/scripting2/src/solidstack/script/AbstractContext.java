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

package solidstack.script;

import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.ValueMap.Entry;


abstract public class AbstractContext
{
	private boolean strictUndefined = true;

	public void setStrictUndefined( boolean strict )
	{
		this.strictUndefined = strict;
	}

	public boolean isStrictUndefined()
	{
		return this.strictUndefined;
	}

	abstract public Value findValue( String name );

	public Value getValue( String name )
	{
		Value v = findValue( name );
		if( v == null )
			return new Undefined( name );
		return v;
	}

	public Object get( String name )
	{
		Value v = findValue( name );
		if( v == null )
			return null;
		return v.value;
	}

	abstract public Variable def( String name, Object value );

	abstract public Value val( String name, Object value );

	public void def( Map<String, Object> values )
	{
		for( java.util.Map.Entry<String, Object> entry : values.entrySet() )
			def( entry.getKey(), entry.getValue() );
	}

	public void set( String name, Object value )
	{
		if( setIfExists( name, value ) )
			return;
		def( name, value );
	}

	public boolean setIfExists( String name, Object value )
	{
		Value v = findValue( name );
		if( v == null )
			return false;
		if( v instanceof Variable )
		{
			v.value = value;
			return true;
		}
		throw new ScriptException( "Cannot assign to value '" + name  + "'" );
	}

//	public Undefined getUndefined( String name )
//	{
//		return new Undefined( name );
//	}

	static public class Value extends Entry implements Lazy
	{
		Object value;

		Value( String name, Object value )
		{
			super( name );

			Assert.notNull( value );
			this.value = value;
		}

		public Object get()
		{
			return this.value;
		}
	}

	static public class Variable extends Value
	{
		Variable( String name, Object value )
		{
			super( name, value );
		}

		public void set( Object value )
		{
			this.value = value;
		}
	}

	public class Undefined extends Variable
	{
		Undefined( String name )
		{
			super( name, Null.INSTANCE );
		}

		@Override
		public Object get()
		{
			if( AbstractContext.this.strictUndefined )
				throw new ScriptException( "'" + getKey() + "' undefined" );
			return null;
		}

		@Override
		public void set( Object value )
		{
			def( getKey(), value );
		}
	}
}
