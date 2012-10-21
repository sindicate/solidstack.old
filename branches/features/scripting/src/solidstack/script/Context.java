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

import solidstack.script.functions.Abs;
import solidstack.script.functions.Length;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Substr;
import solidstack.script.functions.Upper;

public class Context
{
	static public class Value
	{
		String name;
		Object value;
		Value( String name, Object value ) { this.name = name; this.value = value; }
		public Object get() { return this.value; }
	}

	static public class Variable extends Value
	{
		Variable( String name, Object value ) { super( name, value ); }
		public void set( Object value ) { this.value = value; }
	}

	List<Value> values = new ArrayList<Context.Value>();

	{
		this.values.add( new Value( "abs", new Abs() ) );
		this.values.add( new Value( "length", new Length() ) );
		this.values.add( new Value( "print", new Print() ) );
		this.values.add( new Value( "println", new Println() ) );
		this.values.add( new Value( "substr", new Substr() ) );
		this.values.add( new Value( "upper", new Upper() ) );
	}

	public Value getValue( String name )
	{
		for( Value value : this.values )
			if( value.name.equals( name ) )
				return value;
		return null;
	}

	public Object get( String name )
	{
		Value v = getValue( name );
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
		Value v = getValue( name );
		if( v == null )
			return false;
		if( v instanceof Variable )
		{
			v.value = value;
			return true;
		}
		throw new ScriptException( "Cannot assign to value '" + name  + "'" );
	}
}
