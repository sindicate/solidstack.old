/*--
 * Copyright 2005 René M. de Bloois
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

package solidstack.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The SimpleCache does things as simple as possible.
 * <ul>
 * <li>User code is responsible for refreshing a value in the cache whenever the cache signals it.</li>
 * <li>It uses non-blocking retrieval, even if a value in the cache is in the process of being refreshed by another
 * thread. It will temporarily get the old version of the value instead or null if the value does not yet exist in the
 * cache. This means that when the value does not yet exist in the cache, in very rare cases multiple threads could be
 * busy refreshing a specific value, but this should not pose any problem.</li>
 * <li>This cache does not care about memory. It will hold on to every value that gets stored in it. Each hour however,
 * all values that are not refreshed within the last hour will be purged.</li>
 * <li>No extra threads are used. Everything is done in the calling thread.</li>
 * </ul>
 * 
 * @author René de Bloois
 * @since 2005
 */
public class SimpleCache
{
	static private final Logger log = LoggerFactory.getLogger( SimpleCache.class );

	/**
	 * Default cache expiration interval 10 minutes.
	 */
	static public final int DEFAULT_EXPIRATION_MILLIS = 600000;

	/**
	 * Default expiration interval extension 1 minute.
	 */
	static public final int DEFAULT_EXTEND_MILLIS = 60000;

	/**
	 * Default purge interval 1 hour.
	 */
	static public final int DEFAULT_PURGE_INTERVAL_MILLIS = 3600000;

	/**
	 * Default purge age 1 hour.
	 */
	static public final int DEFAULT_PURGE_AGE_MILLIS = 3600000;

	/**
	 * The default global cache.
	 */
	static private final SimpleCache defaultCache = new SimpleCache();

	/**
	 * The cache.
	 */
	private Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

	/**
	 * Current expiration interval.
	 */
	private int expirationMillis;

	/**
	 * Current expiration extension interval.
	 */
	private int extendMillis;

	/**
	 * Current purge interval.
	 */
	private int purgePeriodMillis;

	/**
	 * Current purge age.
	 */
	private int purgeAgeMillis;

	/**
	 * The next purge moment.
	 */
	private long nextPurgeMillis;


	/**
	 * Constructor.
	 */
	public SimpleCache()
	{
		// Set default properties
		this.expirationMillis = DEFAULT_EXPIRATION_MILLIS;
		this.extendMillis = DEFAULT_EXTEND_MILLIS;
		this.purgePeriodMillis = DEFAULT_PURGE_INTERVAL_MILLIS;
		this.purgeAgeMillis = DEFAULT_PURGE_AGE_MILLIS;

		this.nextPurgeMillis = getTime() + this.purgePeriodMillis;
	}

	/**
	 * Gets the default global cache.
	 * 
	 * @return The default global cache.
	 */
	static public SimpleCache getCache()
	{
		return defaultCache;
	}

	/**
	 * Returns the current time in milliseconds. This method could be overridden in a subclass to simulate time in a unit test.
	 * 
	 * @return The current time in milliseconds.
	 */
	protected long getTime()
	{
		return System.currentTimeMillis();
	}

	/**
	 * Store a value with the specified key. The value gets the default expiration interval. Nulls can also be stored in the
	 * cache, but in that case care must be taken to always use {@link #retrieveEntry} to detect if the null value
	 * exists in the cache or has been expired.
	 * 
	 * @param key The key used to store the value in the cache.
	 * @param value The value that should be stored in the cache.
	 * @return The cache entry in which the value is stored.
	 */
	public CacheEntry store( Object value, Object... key )
	{
		long now = getTime();

		String keyString = buildKey( key );
		CacheEntry entry = new CacheEntry( value, now, now + this.expirationMillis );

		synchronized( this.cache )
		{
			this.cache.put( keyString, entry );

			if( now >= this.nextPurgeMillis ) // A long is not thread safe! So we need to do this in the same synchronized block.
			{
				// A CHANCE TO PURGE EVERY SO OFTEN
				this.nextPurgeMillis = now + this.purgePeriodMillis;
				purge( now );
			}
		}

		log.debug( "cached [" + keyString + "]" ); // Logging outside the synchronized block!

		return entry;
	}

	/**
	 * Retrieve a value with the specified key. If the value does not exist or the value is expired this method returns
	 * null. In case the value is expired its expiration time will be extended to give the current thread a chance to
	 * refresh the value. This method can not be used to retrieve null values from the cache because you wouldn't be
	 * able to distinguish between null and expired.
	 * 
	 * @param key The key of the value that should be retrieved.
	 * @return The value with the specified key, or null if not found or expired.
	 */
	public Object retrieve( Object... key )
	{
		CacheEntry entry = retrieveEntry( key );
		if( entry == null )
			return null;
		if( entry.getValue() == null )
			throw new UnsupportedOperationException( "Null values cannot be retrieved with this method, use retrieveEntry() instead" );
		return entry.getValue();
	}

	/**
	 * Retrieve a cache entry corresponding with the specified key.
	 * If the entry does not exist in the cache or is expired this method returns null.
	 * In case the value is expired its expiration time will be extended to give the current thread a chance to
	 * refresh the value. This method should be used whenever null values need to be stored in the cache.
	 * 
	 * @param key The key of the value that should be retrieved.
	 * @return the cache entry with the specified key, or null if not found or expired.
	 */
	public CacheEntry retrieveEntry( Object... key )
	{
		String keyString = buildKey( key );

		long now = getTime();
		boolean expired = false;

		CacheEntry entry;
		synchronized( this.cache )
		{
			entry = this.cache.get( keyString );
			if( entry != null && now >= entry.getExpirationTime() )
			{
				entry.setExpirationTime( now + this.extendMillis );
				expired = true;
				entry = null;
			}
		}

		if( entry != null )
		{
			log.debug( "cache hit [" + keyString + "]" );
			return entry;
		}

		if( expired )
		{
			log.debug( "cache expired [" + keyString + "]" );
			return null;
		}

		log.debug( "cache misssss [" + keyString + "]" );
		return null;
	}

	/**
	 * Purges entries from the cache which are expired.
	 * 
	 * @param now The now.
	 */
	private void purge( long now )
	{
		log.debug( "Purging..." );

		long then = now - this.purgeAgeMillis;
		boolean debug = log.isDebugEnabled();
		List<String> keys = new ArrayList<String>();

		synchronized( this.cache )
		{
			for( Iterator<Map.Entry<String, CacheEntry>> iter = this.cache.entrySet().iterator(); iter.hasNext(); )
			{
				Entry<String, CacheEntry> entry = iter.next();
				if( entry.getValue().getExpirationTime() < then )
				{
					iter.remove();
					if( debug )
						keys.add( entry.getKey() );
				}
			}
		}

		for( String key : keys )
			log.debug( "purged [" + key + "]" );
	}

	/**
	 * Returns the number of milliseconds before a cache entry expires.
	 * 
	 * @return the number of milliseconds before a cache entry expires.
	 */
	public int getExpirationMillis()
	{
		return this.expirationMillis;
	}

	/**
	 * Sets the number of milliseconds before a cache entry expires.
	 * 
	 * @param expirationMillis the number of milliseconds before a cache entry expires.
	 */
	public void setExpirationMillis( int expirationMillis )
	{
		if( expirationMillis < 60000 ) // minimal value of 60 seconds
			expirationMillis = 60000;
		this.expirationMillis = expirationMillis;
	}

	/**
	 * Returns the number of milliseconds the expiration of a cache entry will be extended after the first cache miss for this entry.
	 * This gives the thread that got the cache miss a chance to refresh the data in the cache.
	 * 
	 * @return The number of milliseconds the expiration of a cache entry will be extended after the first cache miss for this entry.
	 */
	public int getExtendMillis()
	{
		return this.extendMillis;
	}

	/**
	 * Sets the number of milliseconds that the expiration of a cache entry will be extended after the first cache miss.
	 * This gives the thread that got the cache miss a chance to refresh the data in the cache.
	 * 
	 * @param extendMillis The number of milliseconds the expiration of a cache entry will be extended after the first cache miss for this entry.
	 */
	public void setExtendMillis( int extendMillis )
	{
		if( extendMillis < 60000 ) // minimal value of 60 seconds
			extendMillis = 60000;
		this.extendMillis = extendMillis;
	}

	/**
	 * Returns the age of expired cache entries before they will be purged.
	 * 
	 * @return The age of expired cache entries before they will be purged.
	 */
	public int getPurgeAge()
	{
		return this.purgeAgeMillis;
	}

	/**
	 * Sets the age of expired cache entries before they will be purged.
	 * 
	 * @param purgeAge The age of expired cache entries before they will be purged.
	 */
	public void setPurgeAge( int purgeAge )
	{
		if( purgeAge < 3600000 ) // minimal value of 3600 seconds
			purgeAge = 3600000;
		this.purgeAgeMillis = purgeAge;
	}

	/**
	 * Returns the purge period, which defines the purge frequency.
	 * 
	 * @return The purge period, which defines the purge frequency.
	 */
	public int getPurgePeriod()
	{
		return this.purgePeriodMillis;
	}

	/**
	 * Sets the purge period, which defines the purge frequency.
	 * 
	 * @param purgePeriod The purge period, which defines the purge frequency.
	 */
	public void setPurgePeriod( int purgePeriod )
	{
		if( purgePeriod < 3600000 ) // minimal value of 3600 seconds
			purgePeriod = 3600000;
		this.purgePeriodMillis = purgePeriod;
	}

	/**
	 * Transform an array of objects to a key.
	 *
	 * @param objects The array of objects.
	 * @return The resulting key.
	 */
	static public String buildKey( Object... objects )
	{
		StringBuilder result = new StringBuilder();
		for( Object object : objects )
		{
			if( result.length() > 0 )
				result.append( ';' );
			if( object == null )
				result.append( "<null>" );
			else
				result.append( object );
		}
		return result.toString();
	}

	/**
	 * A tuple of a value and an expiration time used to store the value in the cache.
	 */
	static public class CacheEntry
	{
		private long stored, expire;
		private Object value;

		/**
		 * Constructor.
		 * 
		 * @param value The value.
		 * @param stored When was the cache entry stored.
		 * @param expire When does the cache entry expire.
		 */
		protected CacheEntry( Object value, long stored, long expire )
		{
			this.stored = stored;
			this.expire = expire;
			this.value = value;
		}

		void setExpirationTime( long expire )
		{
			this.expire = expire;
		}

		/**
		 * Returns the value stored in this cache entry. The value can be null.
		 * 
		 * @return The value stored in this cache entry.
		 */
		public Object getValue()
		{
			return this.value;
		}

		/**
		 * Returns the time when the value was stored in the cache.
		 * 
		 * @return The time when the value was stored in the cache.
		 */
		public long getStoredTime()
		{
			return this.stored;
		}

		/**
		 * Returns the time when the value will expire.
		 * 
		 * @return The time when the value will expire.
		 */
		public long getExpirationTime()
		{
			return this.expire;
		}
	}
}
