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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import solidstack.lang.Assert;

public class Context
{
	private Context parent;

	private List<Value> values = new ArrayList<Context.Value>();

	public Context()
	{
	}

	public Context( Context parent )
	{
		this.parent = parent;
	}

	Value findLocalValue( String name )
	{
		for( Value value : this.values )
			if( value.name.equals( name ) )
				return value;
		return null;
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

	private void removeValue( String name )
	{
		Iterator<Value> i = this.values.iterator();
		while( i.hasNext() )
		{
			Value v = i.next();
			if( v.name.equals( name ) )
				i.remove(); // TODO return
		}
	}

	public void def( String name, Object value )
	{
		removeValue( name );
		this.values.add( new Variable( name, value ) );
	}

	public void val( String name, Object value )
	{
		removeValue( name );
		this.values.add( new Value( name, value ) );
	}

	public void set( String name, Object value )
	{
		if( setIfExists( name, value ) )
			return;
		this.values.add( new Variable( name, value ) );
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

	static public class Value
	{
		String name;
		Object value;

		Value( String name, Object value )
		{
			Assert.notNull( value );
			this.name = name;
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
			throw new ScriptException( "'" + this.name + "' undefined" );
		}

		@Override
		public void set( Object value )
		{
			Context.this.values.add( new Variable( this.name, value ) );
		}
	}
}
