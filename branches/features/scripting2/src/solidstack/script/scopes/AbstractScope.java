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

import solidstack.lang.Assert;
import solidstack.script.ScriptException;
import solidstack.script.objects.Null;
import solidstack.script.scopes.ValueMap.Entry;


abstract public class AbstractScope
{
	abstract public Value findValue( Symbol symbol );

	public Value getValue( Symbol symbol )
	{
		Value v = findValue( symbol );
		if( v == null )
			return new Undefined( symbol );
		return v;
	}

	public Object get( Symbol symbol )
	{
		Value v = findValue( symbol );
		if( v == null )
			return null;
		return v.value;
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
		Value v = findValue( symbol );
		if( v == null )
			return false;
		if( v instanceof Variable )
		{
			v.value = value;
			return true;
		}
		throw new ScriptException( "Cannot assign to value '" + symbol  + "'" );
	}

	static public class Value extends Entry
	{
		Object value;

		Value( Symbol symbol, Object value )
		{
			super( symbol );

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
		Variable( Symbol symbol, Object value )
		{
			super( symbol, value );
		}

		public void set( Object value )
		{
			this.value = value;
		}
	}

	public class Undefined extends Variable
	{
		Undefined( Symbol symbol )
		{
			super( symbol, Null.INSTANCE );
		}

		@Override
		public Object get()
		{
			throw new ScriptException( "'" + getKey() + "' undefined" );
		}

		@Override
		public void set( Object value )
		{
			def( getKey(), value );
		}
	}
}
