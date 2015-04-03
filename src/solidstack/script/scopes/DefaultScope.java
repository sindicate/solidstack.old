/*--
 * Copyright 2012 Ren� M. de Bloois
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

import funny.Symbol;




public class DefaultScope extends AbstractScope
{
	static public final Symbol THIS = Symbol.apply( "this" );

	private Scope parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public DefaultScope()
	{
		def( THIS, this );
	}

	public DefaultScope( Scope parent )
	{
		this();
		this.parent = parent;
	}

	// For testing
	public void clear()
	{
		this.values.clear();
	}

	Value findLocalValue( Symbol symbol )
	{
		return this.values.get( symbol );
	}

	@Override
	public Ref findRef( Symbol symbol )
	{
		Value v = findLocalValue( symbol );
		if( v != null )
			return v;
		if( this.parent != null )
			return this.parent.findRef( symbol );
		return GlobalScope.instance.findRef( symbol );
	}

	@Override
	public Variable def( Symbol symbol, Object value )
	{
		Variable result = new Variable( symbol, value );
		this.values.put( result );
		return result;
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		Value result = new Value( symbol, value );
		this.values.put( result );
		return result;
	}
}
