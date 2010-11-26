/*--
 * Copyright 2010 René M. de Bloois
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

package solidstack.query;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Decorates an {@link Object} array to let it look like a {@link Map}.
 * 
 * @author René M. de Bloois
 */
public class ValuesMap implements Map< String, Object >
{
	private Map< String, Integer > names; // This one is shared by all instances
	private Object[] values;

	/**
	 * Constructor.
	 * 
	 * @param names The names and indexes of the elements in the {@link Object} array.
	 * @param values The values.
	 */
	public ValuesMap( Map< String, Integer > names, Object[] values )
	{
		this.names = names;
		this.values = values;
	}

	public int size()
	{
		return this.values.length;
	}

	public boolean isEmpty()
	{
		return this.values.length == 0;
	}

	public boolean containsKey( Object key )
	{
		if( !( key instanceof String ) )
			throw new IllegalArgumentException( "Expecting a string" );
		String k = ( (String)key ).toLowerCase();
		return this.names.containsKey( k );
	}

	public Object get( Object key )
	{
		if( !( key instanceof String ) )
			throw new IllegalArgumentException( "Expecting a string" );
		String k = ( (String)key ).toLowerCase();
		Integer index = this.names.get( k );
		if( index == null )
			throw new IllegalArgumentException( "Unknown column name: " + key );
		return this.values[ index ];
	}

	public Set< String > keySet()
	{
		return this.names.keySet();
	}

	public Collection< Object > values()
	{
		return new ValuesList( this.values );
	}

	public boolean containsValue( Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object put( String key, Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object remove( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public void putAll( Map< ? extends String, ? extends Object > m )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set< java.util.Map.Entry< String, Object >> entrySet()
	{
		throw new UnsupportedOperationException();
	}
}
