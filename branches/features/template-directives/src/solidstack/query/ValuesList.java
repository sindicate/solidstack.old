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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Decorates an {@link Object} array to make it look like a {@link List}.
 * 
 * @author René M. de Bloois.
 */
public class ValuesList implements List< Object >
{
	private Object[] values;

	/**
	 * Constructor.
	 * 
	 * @param values The {@link Object} array to decorate.
	 */
	public ValuesList( Object[] values )
	{
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

	public boolean contains( Object o )
	{
		throw new UnsupportedOperationException();
	}

	public Iterator< Object > iterator()
	{
		return new ArrayListIterator( this.values );
	}

	public Object[] toArray()
	{
		return this.values;
	}

	public < T > T[] toArray( T[] a )
	{
		throw new UnsupportedOperationException();
	}

	public boolean add( Object e )
	{
		throw new UnsupportedOperationException();
	}

	public boolean remove( Object o )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsAll( Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( Collection< ? extends Object > c )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( int index, Collection< ? extends Object > c )
	{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll( Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll( Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Object get( int index )
	{
		return this.values[ index ];
	}

	public Object set( int index, Object element )
	{
		throw new UnsupportedOperationException();
	}

	public void add( int index, Object element )
	{
		throw new UnsupportedOperationException();
	}

	public Object remove( int index )
	{
		throw new UnsupportedOperationException();
	}

	public int indexOf( Object o )
	{
		throw new UnsupportedOperationException();
	}

	public int lastIndexOf( Object o )
	{
		throw new UnsupportedOperationException();
	}

	public ListIterator< Object > listIterator()
	{
		return new ArrayListIterator( this.values );
	}

	public ListIterator< Object > listIterator( int index )
	{
		throw new UnsupportedOperationException();
	}

	public List< Object > subList( int fromIndex, int toIndex )
	{
		throw new UnsupportedOperationException();
	}
}
