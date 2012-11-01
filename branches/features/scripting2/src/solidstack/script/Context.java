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



public class Context extends AbstractContext
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

	@Override
	public Value findValue( String name )
	{
		Value v = findLocalValue( name );
		if( v != null )
			return v;
		if( this.parent != null )
			return this.parent.findValue( name );
		return GlobalContext.INSTANCE.findLocalValue( name );
	}

	@Override
	public Variable def( String name, Object value )
	{
		Variable result = new Variable( name, value );
		this.values.put( result );
		return result;
	}

	@Override
	public Value val( String name, Object value )
	{
		Value result = new Value( name, value );
		this.values.put( result );
		return result;
	}
}
