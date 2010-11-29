/*--
 * Copyright 2006 René M. de Bloois
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

package solidstack.util;

import java.lang.reflect.Array;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Enables the iteration over Object arrays and primitive arrays.
 * 
 * @author René M. de Bloois
 * @since Aug 12, 2006
 */
public class ArrayListIterator implements ListIterator<Object>
{
	private Object array;
	private int pos;
	private int size;
	private int lastpos;

	/**
	 * Constructor.
	 * 
	 * @param array The array over which you want to iterate.
	 */
	public ArrayListIterator( Object array )
	{
		if( !array.getClass().isArray() )
			throw new IllegalArgumentException( "Given object is not an array" );

		this.array = array;
		this.pos = 0;
		this.size = Array.getLength( array );
		this.lastpos = -1;
	}

	public void add( Object o )
	{
		throw new UnsupportedOperationException();
	}

	public boolean hasNext()
	{
		return this.pos < this.size;
	}

	public boolean hasPrevious()
	{
		return this.pos > 0;
	}

	public Object next()
	{
		if( this.pos >= this.size )
			throw new NoSuchElementException();

		return Array.get( this.array, this.pos++ );
	}

	public int nextIndex()
	{
		return this.pos;
	}

	public Object previous()
	{
		if( this.pos <= 0 )
			throw new NoSuchElementException();

		return Array.get( this.array, --this.pos );
	}

	public int previousIndex()
	{
		return this.pos - 1;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public void set( Object value )
	{
		if( this.lastpos < 0 )
			throw new IllegalStateException();

		Array.set( this.array, this.lastpos, value );
	}
}
