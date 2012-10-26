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

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * A map that stores the values without an intermediary (entry) object. The values itself must be subclasses of the {@link Entry}.
 *
 * @param <T> The type of the values to be stored in this map.
 */
public class ValueMap<T extends ValueMap.Entry> implements Map<String, T>
{
	static public final float LOAD_FACTOR = 4f;

	private Entry[] entries = new Entry[ 4 ];
	private int size;
	private int threshold = (int)( this.entries.length * LOAD_FACTOR );


	public T get( Object key )
	{
		if( key == null )
			throw new NullPointerException( "key" );

		int hash = key.hashCode();
		int index = hash & this.entries.length - 1;

		Entry entry = this.entries[ index ];
		while( entry != null ) // Loop till we find it
		{
			if( entry.key.equals( key ) )
				return (T)entry;
			entry = entry.next;
		}

		return null;
	}

	public T put( String key, T value )
	{
		if( key == null )
			throw new NullPointerException( "key" );

		value.key = key;

		int hash = key.hashCode();
		int index = hash & this.entries.length - 1;

		Entry entry = this.entries[ index ];
		Entry last = null;
		while( entry != null ) // Loop till we find it
		{
			if( entry.key.equals( key ) )
			{
				value.next = entry.next; // Replace link with the new entry
				if( last == null )
				{
					this.entries[ index ] = value; // It's the first one
					return (T)entry;
				}
				last.next = value; // It's somewhere in the middle or at the end
				return (T)entry;
			}

			last = entry;
			entry = entry.next;
		}

		// Not found
		value.next = null; // Force null. It may be set already during resizing.
		if( last == null )
			this.entries[ index ] = value; // It's the only entry
		else
			last.next = value; // At the end

		if( ++this.size > this.threshold )
			resize();

		return null;
	}

	public T remove( Object key )
	{
		if( key == null )
			throw new NullPointerException( "key" );

		int hash = key.hashCode();
		int index = hash & this.entries.length - 1;

		Entry entry = this.entries[ index ];
		Entry last = null;
		while( entry != null ) // Loop till we find it
		{
			if( entry.key.equals( key ) )
			{
				this.size--;
				if( last == null )
				{
					this.entries[ index ] = entry.next; // It's the first one
					return (T)entry;
				}
				last.next = entry.next; // It's somewhere in the middle or at the end
				return (T)entry;
			}

			last = entry;
			entry = entry.next;
		}

		// Not found
		return null;
	}

	public int size()
	{
		return this.size;
	}

	public int largestBucketSize()
	{
		int result = 0;
		for( Entry entry : this.entries )
		{
			int count = 0;
			while( entry != null )
			{
				count++;
				entry = entry.next;
			}
			if( count > result )
				result = count;
		}
		return result;
	}

	public int emptyBucketCount()
	{
		int result = 0;
		for( Entry entry : this.entries )
		{
			int count = 0;
			while( entry != null )
			{
				count++;
				entry = entry.next;
			}
			if( count == 0 )
				result++;
		}
		return result;
	}

	public float averageChainingUnknownKeys()
	{
		int result = 0;
		for( Entry entry : this.entries )
		{
			int count = 0;
			while( entry != null )
			{
				count++;
				entry = entry.next;
			}
			result += count;
		}
		return (float)result / this.entries.length;
	}

	public float averageChainingKnownKeys()
	{
		int result = 0;
		for( Entry entry : this.entries )
		{
			int count = 0;
			while( entry != null )
			{
				count++;
				result += count;
				entry = entry.next;
			}
		}
		return (float)result / this.size;
	}

	private void resize()
	{
		System.out.println( "size = " + this.size );

		Entry[] oldEntries = this.entries;

		this.entries = new Entry[ this.entries.length * 2 ];
		this.size = 0;
		this.threshold *= 2;

		System.out.println( "resizing to " + this.entries.length );

		for( Entry entry : oldEntries )
		{
			while( entry != null )
			{
				Entry next = entry.next;
				put( entry.key, (T)entry );
				entry = next;
			}
		}
	}

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsValue( Object value )
	{
		throw new UnsupportedOperationException();
	}

	public void putAll( Map<? extends String, ? extends T> m )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set<String> keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection<T> values()
	{
		throw new UnsupportedOperationException();
	}

	public Set<java.util.Map.Entry<String, T>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * An entry in the ValueMap. Values must be subclasses of this class.
	 */
	static public class Entry
	{
		private Entry next; // private to prevent name shadowing in subclasses
		private String key; // private to prevent name shadowing in subclasses
	}
}
