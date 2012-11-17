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




public class Scope extends AbstractScope
{
	static public final Symbol THIS = Symbol.forString( "this" );

	private AbstractScope parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public Scope()
	{
		def( THIS, this );
	}

	public Scope( AbstractScope parent )
	{
		this();
		this.parent = parent;
	}

	Value findLocalValue( Symbol symbol )
	{
		return this.values.get( symbol );
	}

	@Override
	public Value findValue( Symbol symbol )
	{
		Value v = findLocalValue( symbol );
		if( v != null )
			return v;
		if( this.parent != null )
			return this.parent.findValue( symbol );
		return GlobalScope.INSTANCE.findLocalValue( symbol );
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
