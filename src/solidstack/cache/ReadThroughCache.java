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

package solidstack.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidbase.core.SystemException;
import solidstack.cache.ReadThroughCache.CacheEntry.STATE;


/**
 * A read-through cache.
 * 
 * @author René de Bloois
 * @since 2012
 */
public class ReadThroughCache
{
	static private final Logger log = LoggerFactory.getLogger( ReadThroughCache.class );

	/**
	 * Default cache expiration 10 minutes.
	 */
	static public final int DEFAULT_EXPIRATION_MILLIS = 600000;

	/**
	 * Default wait timeout 1 minute.
	 */
	static public final int DEFAULT_WAIT_TIMEOUT_MILLIS = 60000;

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
	static private final ReadThroughCache defaultCache = new ReadThroughCache();

	/**
	 * The cache.
	 */
	private Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

	/**
	 * Current expiration interval.
	 */
	private int expirationMillis;

	/**
	 * Current wait timeout.
	 */
	private int waitTimeoutMillis;

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

	private boolean nonBlocking = true;


	/**
	 * Gets the default global cache.
	 * 
	 * @return The default global cache.
	 */
	static public ReadThroughCache getCache()
	{
		return defaultCache;
	}

	/**
	 * Constructor.
	 */
	public ReadThroughCache()
	{
		// Set default properties
		this.expirationMillis = DEFAULT_EXPIRATION_MILLIS;
		this.purgePeriodMillis = DEFAULT_PURGE_INTERVAL_MILLIS;
		this.purgeAgeMillis = DEFAULT_PURGE_AGE_MILLIS;
		this.waitTimeoutMillis = DEFAULT_WAIT_TIMEOUT_MILLIS;

		this.nextPurgeMillis = getTime() + this.purgePeriodMillis;
	}

	/**
	 * Returns the wait timeout. The wait timeout limits the time that one thread waits for another thread to reload the data into the cache.
	 * 
	 * @return The wait timeout.
	 */
	public int getWaitTimeoutMillis()
	{
		return this.waitTimeoutMillis;
	}

	/**
	 * Sets the wait timeout. The wait timeout limits the time that one thread waits for another thread to reload the data into the cache.
	 * 
	 * @param waitTimeoutMillis The wait timeout.
	 */
	public void setWaitTimeoutMillis( int waitTimeoutMillis )
	{
		if( waitTimeoutMillis < 0 )
			throw new IllegalArgumentException( "waitTimeoutMillis can't be negative" );
		this.waitTimeoutMillis = waitTimeoutMillis;
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
		if( expirationMillis < 0 )
			throw new IllegalArgumentException( "expirationMillis can't be negative" );
		this.expirationMillis = expirationMillis;
	}

	/**
	 * Returns the purge period, which defines the purge frequency.
	 * 
	 * @return The purge period, which defines the purge frequency.
	 */
	public int getPurgePeriodMillis()
	{
		return this.purgePeriodMillis;
	}

	/**
	 * Sets the purge period, which defines the purge frequency.
	 * 
	 * @param purgePeriod The purge period, which defines the purge frequency.
	 */
	public void setPurgePeriodMillis( int purgePeriod )
	{
		if( purgePeriod < 0 )
			throw new IllegalArgumentException( "purgePeriod can't be negative" );
		this.purgePeriodMillis = purgePeriod;
	}

	/**
	 * Returns the age of expired cache entries before they will be purged.
	 * 
	 * @return The age of expired cache entries before they will be purged.
	 */
	public int getPurgeAgeMillis()
	{
		return this.purgeAgeMillis;
	}

	/**
	 * Sets the age of expired cache entries before they will be purged.
	 * 
	 * @param purgeAge The age of expired cache entries before they will be purged.
	 */
	public void setPurgeAgeMillis( int purgeAge )
	{
		if( purgeAge < 0 )
			throw new IllegalArgumentException( "purgeAge can't be negative" );
		this.purgeAgeMillis = purgeAge;
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

	// ! No logging inside synchronized blocks !

	/**
	 * Get a value from the cache. If the value is not found or the value is expired, the loader is called to get a new
	 * value to store in the cache. During load, other threads that are trying to get the same value are blocked until
	 * loading is finished.
	 * 
	 * @param loader The loader to load values.
	 * @param key The key into the cache.
	 * @return The value belonging to the key.
	 */
	// LOADED -> RELOADING protected by locking this.cache
	public <T> T get( final Loader loader, Object... key )
	{
		final String keyString = buildKey( key );

		CacheEntry result;
		boolean load = false;
//		boolean reload = false;

		synchronized( this.cache )
		{
			long now = getTime(); // Get the time after entering the synchronized block

			if( now >= this.nextPurgeMillis ) // A long is not thread safe! So we need to do this in the same synchronized block.
			{
				// A CHANCE TO PURGE EVERY SO OFTEN
				this.nextPurgeMillis = now + this.purgePeriodMillis;
				purge( now );
			}

			result = this.cache.get( keyString );
			if( result == null )
			{
				result = new CacheEntry( STATE.LOADING, null, now, now + this.expirationMillis );
				this.cache.put( keyString, result );
				load = true;
			}
			else
			{
				switch( result.getState() )
				{
					case LOADED:
						if( now > result.getExpirationTime() )
						{
							result = new CacheEntry( STATE.RELOADING, result.getValue(), now, now + this.expirationMillis );
							this.cache.put( keyString, result );
							load = /* reload = */ true;
						}
						else
						{
							log.debug( "hit [" + keyString + "]" ); // TODO No logging inside synchronized block
							return extractValue( result );
						}
						break;

					case RELOADING:
						// TODO Expiration
						if( this.nonBlocking )
							return extractValue( result );
						break;

					case FAILED:
						// TODO Expiration
						return extractValue( result );

					case LOADING:
						// TODO Expiration
				}
			}
		}

		// 1. result now contains a CacheEntry
		// 2. load indicates that we need to load

		if( load )
		{
			if( this.nonBlocking && result.getState() == STATE.RELOADING )
			{
				final CacheEntry _result = result;
				new Thread()
				{
					@Override
					public void run()
					{
						load( _result, keyString, loader );
					}
				}.start();
				return extractValue( result );
			}
			return load( result, keyString, loader );
		}

		log.debug( "waiting [" + keyString + "]" );
		synchronized( result )
		{
			// Don't wait if loading has finished already, we won't get a signal then
			if( result.getState() == STATE.LOADING || result.getState() == STATE.RELOADING )
			{
				try
				{
					wait( result, this.waitTimeoutMillis );
				}
				catch( InterruptedException e )
				{
					throw new CacheTimeoutException( "Interrupted during waiting for the cache entry to be reloaded [" + keyString + "]" );
				}
				if( result.getState() == STATE.LOADING || result.getState() == STATE.RELOADING )
					throw new CacheTimeoutException( "Timed out waiting for the cache entry to be reloaded [" + keyString + "]" );
			}
			return extractValue( result );
		}
	}

	// LOADING/RELOADING -> LOADED/FAILED protected by lock on CacheEntry
	<T> T load( CacheEntry entry, String key, Loader loader )
	{
		T value = null;
		Throwable throwable = null;
		try
		{
			try
			{
				// TODO Needs to be protected
				if( entry.getState() == STATE.LOADING )
					log.debug( "miss:loading [" + key + "]" );
				else
					log.debug( "expired:reloading [" + key+ "]" );

				value = (T)loader.load();
			}
			catch( Throwable t )
			{
				throwable = t;
			}
		}
		finally
		{
			// First notify all
			synchronized( entry )
			{
				entry.notifyAll();
				long now = getTime();
				if( throwable != null )
					entry.failed( throwable, now, now + this.expirationMillis );
				else
					entry.loaded( value, now, now + this.expirationMillis );
			}
			if( throwable != null )
			{
				log.debug( "failed [" + key + "]" );
				if( throwable instanceof RuntimeException )
					throw (RuntimeException)throwable;
				if( throwable instanceof Error )
					throw (Error)throwable;
				throw new SystemException( throwable );
			}
			log.debug( "loaded [" + key + "]" );
		}
		return value;
	}

	static private <T> T extractValue( CacheEntry entry )
	{
		if( entry.getState() == STATE.FAILED )
		{
			Throwable t = (Throwable)entry.getValue();
			if( t instanceof RuntimeException )
				throw (RuntimeException)t;
			if( t instanceof Error )
				throw (Error)t;
			throw new SystemException( t );
		}

		if( entry.getState() == STATE.LOADING )
			throw new IllegalStateException( "CacheEntry is still loading" );

		return (T)entry.getValue();
	}

	// Pure technical wait implementation
	private void wait( CacheEntry entry, int timeout ) throws InterruptedException
	{
		if( timeout <= 0 )
		{
			do
				entry.wait();
			while( entry.getState() == STATE.LOADING );
		}
		else
		{
			long now = getTime();
			long stop = now + timeout;
			do
			{
				entry.wait( stop - now );
				now = getTime();
			}
			while( entry.getState() == STATE.LOADING && now < stop );
		}
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

		for( String key : keys )
			log.debug( "purged [" + key + "]" );
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
		static enum STATE { LOADING, RELOADING, LOADED, FAILED }

		private long stored, expire;
		private Object value;
		private STATE state;

		protected CacheEntry( STATE state, Object value, long stored, long expiration )
		{
			this.state = state;
			this.value = value;
			this.stored = stored;
			this.expire = expiration;
		}

//		public void reloading()
//		{
//			this.state = STATE.RELOADING;
//		}

//		/**
//		 * Constructor.
//		 *
//		 * @param value The value.
//		 * @param stored When was the cache entry stored.
//		 * @param expire When does the cache entry expire.
//		 */
//		protected CacheEntry( Object value, long stored, long expire )
//		{
//			loaded( value, stored, expire );
//		}

//		void setExpirationTime( long expire )
//		{
//			this.expire = expire;
//		}

		public STATE getState()
		{
			return this.state;
		}

		public void loaded( Object value, long stored, long expire )
		{
			this.stored = stored;
			this.expire = expire;
			this.value = value;
			this.state = STATE.LOADED;
		}

		public void failed( Throwable throwable, long stored, long expire )
		{
			this.stored = stored;
			this.expire = expire;
			this.value = throwable;
			this.state = STATE.FAILED;
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

//		/**
//		 * Returns the time when the value was stored in the cache.
//		 *
//		 * @return The time when the value was stored in the cache.
//		 */
//		public long getStoredTime()
//		{
//			return this.stored;
//		}

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

	static public interface Loader
	{
		Object load();
	}
}
