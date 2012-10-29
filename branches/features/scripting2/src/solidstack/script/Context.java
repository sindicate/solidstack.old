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

import solidstack.lang.Assert;
import solidstack.script.ValueMap.Entry;


public class Context
{
	private Context parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public Context()
	{
		def( "this", this );
	}

	public Context( Context parent )
	{
		this();
		this.parent = parent;
	}

	Value findLocalValue( String name )
	{
		return this.values.get( name );
	}

	private Value findValue( String name )
	{
		Value v = findLocalValue( name );
		if( v != null )
			return v;
		if( this.parent != null )
			return this.parent.findValue( name );
		return GlobalContext.INSTANCE.findLocalValue( name );
	}

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

	public void def( String name, Object value )
	{
		this.values.put( name, new Variable( value ) );
	}

	public void val( String name, Object value )
	{
		this.values.put( name, new Variable( value ) );
	}

	public void set( String name, Object value )
	{
		if( setIfExists( name, value ) )
			return;
		this.values.put( name, new Variable( value ) );
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

	static public class Value extends Entry
	{
		Object value;

		Value( Object value )
		{
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
		Variable( Object value )
		{
			super( value );
		}

		public void set( Object value )
		{
			this.value = value;
		}
	}

	public class Undefined extends Variable
	{
		private String name;

		Undefined( String name )
		{
			super( Null.INSTANCE );
			this.name = name;
		}

		@Override
		public Object get()
		{
			throw new ScriptException( "'" + this.name + "' undefined" );
		}

		@Override
		public void set( Object value )
		{
			Context.this.values.put( this.name, new Variable( value ) );
		}
	}
}
