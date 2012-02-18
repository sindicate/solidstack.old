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


/**
 * A read-through cache.
 * 
 * @author René de Bloois
 * @since 2012
 */
public class ReadThroughCache
{
	static final Logger log = LoggerFactory.getLogger( ReadThroughCache.class );

	/**
	 * Default cache expiration 10 minutes.
	 */
	static public final int DEFAULT_EXPIRATION_MILLIS = 600000;

	/**
	 * Grace period 2 minutes.
	 */
	static public final int DEFAULT_GRACE_PERIOD_MILLIS = 120000;

	/**
	 * Default loading cache expiration 1 minute.
	 */
	static public final int DEFAULT_LOAD_TIMEOUT_MILLIS = 60000;

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
	private int expirationMillis = DEFAULT_EXPIRATION_MILLIS;

	/**
	 * Current grace period.
	 */
	private int gracePeriodMillis = DEFAULT_GRACE_PERIOD_MILLIS;

	/**
	 * Current load timeout.
	 */
	private int loadTimeoutMillis = DEFAULT_LOAD_TIMEOUT_MILLIS;

	/**
	 * Current wait timeout.
	 */
	private int waitTimeoutMillis = DEFAULT_WAIT_TIMEOUT_MILLIS;

	/**
	 * Current purge interval.
	 */
	private int purgePeriodMillis = DEFAULT_PURGE_INTERVAL_MILLIS;

	/**
	 * Current purge age.
	 */
	private int purgeAgeMillis = DEFAULT_PURGE_AGE_MILLIS;

	/**
	 * The next purge moment.
	 */
	private long nextPurgeMillis = System.currentTimeMillis() + this.purgePeriodMillis;

	// TODO Ability to overrule blocking/nonblocking
	private boolean nonBlocking = false;


	/**
	 * Gets the default global cache.
	 * 
	 * @return The default global cache.
	 */
	static public ReadThroughCache getCache()
	{
		return defaultCache;
	}


	// TODO Give a name to the cache


	// ---------- Getters & Setters

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

	public int getGracePeriodMillis()
	{
		return this.gracePeriodMillis;
	}

	public void setGracePeriodMillis( int gracePeriodMillis )
	{
		this.gracePeriodMillis = gracePeriodMillis;
	}

	public int getLoadTimeoutMillis()
	{
		return this.loadTimeoutMillis;
	}

	public void setLoadTimeoutMillis( int loadTimeoutMillis )
	{
		this.loadTimeoutMillis = loadTimeoutMillis;
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

	// END ------ Getters & Setters



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
	@SuppressWarnings( "unchecked" )
	public <T> T get( final Loader<T> loader, Object... key )
	{
		final String keyString = buildKey( key );

		CacheEntry result;
		CacheEntry loading = null;

		long now;

		// Purge
		boolean purge = false;
		synchronized( this )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block
			if( now >= this.nextPurgeMillis ) // A long is not thread safe!
			{
				this.nextPurgeMillis = now + this.purgePeriodMillis;
				purge = true;
			}
		}
		if( purge )
			purge( now );

		// Read and write the cache
		String logthis = null;
		synchronized( this.cache )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block

			result = this.cache.get( keyString );

			if( result == null )
			{
				loading = new LoadingCacheEntry( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
				this.cache.put( keyString, loading );
				logthis = "miss [{}]";
			}
			else if( now >= result.getExpirationTime() )
			{
				if( result instanceof LoadedCacheEntry )
				{
					if( now < result.getExpirationTime() + this.gracePeriodMillis )
						loading = new ReloadingCacheEntry( ( (LoadedCacheEntry)result ).getValue(), now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
					else
						loading = new LoadingCacheEntry( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
					this.cache.put( keyString, loading );
					logthis = "expired [{}]";
				}
				else if( result instanceof FailedCacheEntry )
				{
					loading = new LoadingCacheEntry( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
					this.cache.put( keyString, loading );
					logthis = "fail expired [{}]";
				}
				else
				{
					Throwable t = new IllegalStateException( "LoadingCacheEntry expired in cache" );
					CacheEntry failed = new FailedCacheEntry( t, now, now + this.expirationMillis );
					this.cache.put( keyString, failed );
					( (LoadingCacheEntry)result ).setResult( failed );
					result = failed;
					logthis = "load expired [{}]";
				}
			}
		}

		if( logthis != null )
			log.debug( logthis, keyString );

		// 1. result contains a CacheEntry or null
		// 2. loading contains an entry we need to load and overrules result

		if( loading != null )
		{
			if( this.nonBlocking && loading instanceof ReloadingCacheEntry )
			{
				log.debug( "background load [" + keyString + "]" );
				final ReloadingCacheEntry _loading = (ReloadingCacheEntry)loading;
				// TODO Should this thread be managed?
				new Thread()
				{
					@Override
					public void run()
					{
						load( _loading, keyString, loader );
					}
				}.start();
				log.debug( "use old [" + keyString + "]" );
				return (T)_loading.getOldValue();
			}

			log.debug( "blocking load [" + keyString + "]" );
			return load( (LoadingCacheEntry)loading, keyString, loader );
		}

		// 1. result contains a CacheEntry

		if( result instanceof LoadingCacheEntry )
		{
			if( this.nonBlocking && result instanceof ReloadingCacheEntry )
			{
				log.debug( "use old [" + keyString + "]" );
				return (T)( (ReloadingCacheEntry)result ).getOldValue();
			}
			log.debug( "waiting [" + keyString + "]" );
			try
			{
				result = ( (LoadingCacheEntry)result ).getResult( keyString ); // Blocking
			}
			catch( InterruptedException e )
			{
				throw (ThreadDeath)new ThreadDeath().initCause( e );
			}
			log.debug( "ready [" + keyString + "]" );
		}

		// 1. result contains a Loaded or a Failed

		if( result instanceof LoadedCacheEntry )
		{
			log.debug( "hit [" + keyString + "]" );
			return (T)( (LoadedCacheEntry)result ).getValue();
		}

		Throwable t = ( (FailedCacheEntry)result ).getThrowable();
		if( t instanceof RuntimeException )
			throw (RuntimeException)t;
		if( t instanceof Error )
			throw (Error)t;
		throw new SystemException( t );
	}

	/**
	 * Call the loader and put the result in the cache.
	 * 
	 * @param entry The entry to load.
	 * @param key The key of the entry to load.
	 * @param loader The loader to use.
	 * @return The value loaded.
	 */
	<T> T load( LoadingCacheEntry entry, String keyString, Loader<T> loader )
	{
		T value = null;
		Throwable throwable = null;
		try
		{
			try
			{
				value = loader.load();
				log.debug( "load success [" + keyString + "]" );
			}
			catch( Throwable t )
			{
				throwable = t;
				log.debug( "load failed [" + keyString + "]" );
			}
		}
		finally
		{
			long now = System.currentTimeMillis();
			CacheEntry result;
			if( throwable != null )
				result = new FailedCacheEntry( throwable, now, now + this.expirationMillis );
			else
				result = new LoadedCacheEntry( value, now, now + this.expirationMillis );

			synchronized( this.cache )
			{
				this.cache.put( keyString, result );
			}

			entry.setResult( result ); // Notifies all waiting threads
		}
		return value;
	}

	/**
	 * Purges entries from the cache which have aged a lot.
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
				if( entry.getValue().getStoredTime() < then )
				{
					iter.remove();
					if( debug )
						keys.add( entry.getKey() );
				}
			}
		}

		// TODO Also give warnings when a (Re)LoadingCacheEntry is purged
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
				result.append( object ); // TODO Escape ; character
		}
		return result.toString();
	}


	// ---------- Here are the cache entry types

	/**
	 * A tuple of a value and an expiration time used to store the value in the cache.
	 */
	static abstract private class CacheEntry
	{
		private long stored, expire;

		protected CacheEntry( long stored, long expiration )
		{
			this.stored = stored;
			this.expire = expiration;
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

	static private class LoadingCacheEntry extends CacheEntry
	{
		private CacheEntry result;
		private int waitTimeoutMillis;

		LoadingCacheEntry( long stored, long expiration, int waitTimeoutMillis )
		{
			super( stored, expiration );
			this.waitTimeoutMillis = waitTimeoutMillis;
		}

		synchronized public CacheEntry getResult( String key ) throws InterruptedException
		{
			// Don't wait if loading has finished already, we won't get a notify then
			if( this.result != null )
				return this.result;

			if( this.waitTimeoutMillis <= 0 )
			{
				do
					wait();
				while( this.result == null );
			}
			else
			{
				long now = System.currentTimeMillis();
				long stop = now + this.waitTimeoutMillis;
				do
				{
					wait( stop - now );
					now = System.currentTimeMillis();
				}
				while( this.result == null && now < stop );
			}

			if( this.result == null )
				throw new CacheTimeoutException( "Timed out waiting for the cache entry to be (re)loaded [" + key + "]" );

			return this.result;
		}

		synchronized public void setResult( CacheEntry result )
		{
			notifyAll(); // First notify all

			if( this.result != null )
				throw new IllegalStateException( "result is already filled" );
			this.result = result;
		}
	}

	static private class ReloadingCacheEntry extends LoadingCacheEntry
	{
		private Object oldValue;

		ReloadingCacheEntry( Object oldValue, long stored, long expiration, int waitTimeoutMillis )
		{
			super( stored, expiration, waitTimeoutMillis );
			this.oldValue = oldValue;
		}

		/**
		 * Returns the value stored in this cache entry. The value can be null.
		 * 
		 * @return The value stored in this cache entry.
		 */
		public Object getOldValue()
		{
			return this.oldValue;
		}
	}

	static private class LoadedCacheEntry extends CacheEntry
	{
		private Object value;

		LoadedCacheEntry( Object value, long stored, long expiration )
		{
			super( stored, expiration );
			this.value = value;
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
	}

	static private class FailedCacheEntry extends CacheEntry
	{
		private Throwable throwable;

		FailedCacheEntry( Throwable throwable, long stored, long expiration )
		{
			super( stored, expiration );
			this.throwable = throwable;
		}

		public Throwable getThrowable()
		{
			return this.throwable;
		}
	}

	static public interface Loader<T>
	{
		T load();
	}
}
