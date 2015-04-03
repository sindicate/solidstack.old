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

import java.util.Map;

import solidstack.script.scopes.ValueMap.Entry;
import funny.Symbol;


abstract public class AbstractScope implements Scope
{
	abstract public Object get( Symbol symbol );
	abstract protected void set0( Symbol symbol, Object value );

	abstract public void var( Symbol symbol, Object value );
	abstract public void val( Symbol symbol, Object value );

	public void set( Symbol symbol, Object value )
	{
		try
		{
			set0( symbol, value );
		}
		catch( UndefinedException e )
		{
			var( symbol, value );
		}
	}

	public void setAll( Map<String, ? extends Object> parameters )
	{
		for( java.util.Map.Entry<String, ? extends Object> entry : parameters.entrySet() )
			set( Symbol.apply( entry.getKey() ), entry.getValue() );
	}

	static public class Value extends Entry
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
}
