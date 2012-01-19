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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


/**
 * A wrapper around a list of arrays.
 * 
 * @author René M. de Bloois
 */
public class ResultList implements List< Map< String, Object > >, Serializable
{
	private List< Object[] > list;
	private Map< String, Integer > names;

	/**
	 * Constructor.
	 * 
	 * @param list The list of arrays.
	 * @param names The name to index map.
	 */
	public ResultList( List< Object[] > list, Map< String, Integer > names )
	{
		this.list = list;
		this.names = names;
	}

	public Map< String, Object > get( int index )
	{
		return new ValuesMap( this.names, this.list.get( index ) );
	}

	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	public Iterator< Map< String, Object >> iterator()
	{
		return new ResultListIterator( this.list.listIterator(), this.names );
	}

	public ListIterator< Map< String, Object >> listIterator()
	{
		return new ResultListIterator( this.list.listIterator(), this.names );
	}

	public ListIterator< Map< String, Object >> listIterator( int index )
	{
		return new ResultListIterator( this.list.listIterator( index ), this.names );
	}

	public int size()
	{
		return this.list.size();
	}

	public boolean add( Map< String, Object > arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void add( int arg0, Map< String, Object > arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( Collection< ? extends Map< String, Object >> arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( int arg0, Collection< ? extends Map< String, Object >> arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean contains( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsAll( Collection< ? > arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public int indexOf( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public int lastIndexOf( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean remove( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Map< String, Object > remove( int arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll( Collection< ? > arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll( Collection< ? > arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Map< String, Object > set( int arg0, Map< String, Object > arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public List< Map< String, Object >> subList( int arg0, int arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public Map<String,Object>[] toArray()
	{
		throw new UnsupportedOperationException();
//		@SuppressWarnings( "unchecked" )
//		Map< String, Object>[] result = new Map[ this.list.size() ];
//		Iterator< Map< String, Object > > i = iterator();
//		int j = 0;
//		while( i.hasNext() )
//			result[ j++ ] = i.next();
//		return result;
	}

	public < T > T[] toArray( T[] arg0 )
	{
		throw new UnsupportedOperationException();
	}
}
