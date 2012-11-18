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

import java.util.Collection;
import java.util.Map;
import java.util.Set;



/**
 * A map that stores the values without an intermediary (entry) object. The values itself must be subclasses of the {@link Entry}.
 *
 * @param <T> The type of the values to be stored in this map.
 */
public class ValueMap<T extends ValueMap.Entry> implements Map<Symbol, T>
{
	static public final float LOAD_FACTOR = 4f;

	private Entry[] entries = new Entry[ 4 ];
	private int size;
	private int threshold = (int)( this.entries.length * LOAD_FACTOR );


	// TODO Work with Identifier instead of String. That way we can pre-compute the hash.
	public T get( Object key )
	{
		if( key == null )
			throw new NullPointerException( "key" );
		if( !( key instanceof Symbol ) )
			throw new IllegalArgumentException( "Only symbols can be keys" );

		Symbol symbol = (Symbol)key;
		int hash = symbol.hashCode();
		int index = hash & this.entries.length - 1;

		Entry entry = this.entries[ index ];
		while( entry != null ) // Loop till we find it
		{
			if( entry.isKeyEqual( symbol ) )
				return (T)entry;
			entry = entry.___next;
		}

		return null;
	}

	public T put( Symbol key, T value )
	{
		if( key == null )
			throw new NullPointerException( "key" );
		if( !key.equals( value.getKey() ) )
			throw new IllegalArgumentException( "keys don't match" );

		return put( value );
	}

	public T put( T value )
	{
		int hash = value.getKeyHashCode();
		int index = hash & this.entries.length - 1;

		Entry entry = this.entries[ index ];
		Entry last = null;
		while( entry != null ) // Loop till we find it
		{
			if( entry.isKeyEqual( value ) )
			{
				value.___next = entry.___next; // Replace link with the new entry
				if( last == null )
				{
					this.entries[ index ] = value; // It's the first one
					return (T)entry;
				}
				last.___next = value; // It's somewhere in the middle or at the end
				return (T)entry;
			}

			last = entry;
			entry = entry.___next;
		}

		// Not found
		value.___next = null; // Force null. It may be set already during resizing.
		if( last == null )
			this.entries[ index ] = value; // It's the only entry
		else
			last.___next = value; // At the end

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
			if( entry.___key.equals( key ) )
			{
				this.size--;
				if( last == null )
				{
					this.entries[ index ] = entry.___next; // It's the first one
					return (T)entry;
				}
				last.___next = entry.___next; // It's somewhere in the middle or at the end
				return (T)entry;
			}

			last = entry;
			entry = entry.___next;
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
				entry = entry.___next;
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
				entry = entry.___next;
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
				entry = entry.___next;
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
				entry = entry.___next;
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
				Entry next = entry.___next;
				put( (T)entry );
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

	public void putAll( Map<? extends Symbol, ? extends T> m )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set<Symbol> keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection<T> values()
	{
		throw new UnsupportedOperationException();
	}

	public Set<java.util.Map.Entry<Symbol, T>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * An entry in the ValueMap. Values must be subclasses of this class.
	 */
	static public class Entry
	{
		// Can't make this private, else it won't compile on Java 7. Added ___ to prevent name shadowing by subclasses.
		private Entry ___next;
		private Symbol symbol;
		private String ___key;
		private int hashCode;
		// TODO Replace key/hashCode with symbol when it is available during the isKeyEqual execution

		protected Entry( Symbol symbol )
		{
			if( symbol instanceof TempSymbol )
			{
				this.___key = symbol.toString();
				this.hashCode = symbol.hashCode();
			}
			else
				this.symbol = symbol;
		}

		public Symbol getKey()
		{
			if( this.symbol != null )
				return this.symbol;
			return new TempSymbol( this.___key, this.hashCode );
		}

		public String getName()
		{
			if( this.symbol != null )
				return this.symbol.toString();
			return this.___key;
		}

		int getKeyHashCode()
		{
			if( this.symbol != null )
				return this.symbol.hashCode();
			return this.hashCode;
		}

		boolean isKeyEqual( Entry other )
		{
			if( other.symbol != null || this.symbol != null )
				return this.symbol == other.symbol;
			return getName().equals( other.getName() );
		}

		boolean isKeyEqual( Symbol symbol )
		{
			if( this.symbol != null )
				return this.symbol.equals( symbol );
			return getName().equals( symbol.toString() );
		}
	}
}
