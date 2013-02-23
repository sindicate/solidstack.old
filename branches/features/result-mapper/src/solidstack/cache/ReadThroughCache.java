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

import solidstack.lang.ThreadInterrupted;


/*
 * Thread safety:
 * 1. All access to the central map containing the cache is synchronized.
 * 2. All access to the nextPurgeMillis field is synchronized.
 * 3. All primitive fields are final or volatile.
 * 4. The Loaded and Failed cache entries are immutable.
 * 5. The Loading and Reloading cache entries are immutable except for the result field which gets assigned only once.
 * 6. Access to the result field of Loading and Reloading is synchronized.
 * 7. The values stored in the cache are NOT threadsafe!
 *
 * Deadlock:
 * 1. There are no nested synchronized block.
 * 2. There are no multiple locks needed.
 *
 * Starvation:
 * TODO
 *
 * No leakage.
 * Leakage concern 1: Object or threads that are not used any more but references to them still exist.
 * 1.1. There are no thread references.
 * 1.2. The only references to cache entries are:
 * 1.2.1. From the central map to the cache entries.
 * 1.2.2. From the Loading and Reloading cache entries to the Loaded or Failed cache entries.
 * 1.3. A purge routine runs regularly to remove old entries from the cache.
 * Leakage concern 2: Loading and Reloading cache entries that survive in the cache even though the loading thread died.
 * 2.1. The loading routine has a catch Throwable clause and is carefully written not to trigger exceptions itself.
 * Leakage concern 3: Loading threads that survive even though its Loading or Reloading cache entry is removed from the cache.
 * 3.1. No care is taken concerning this category. The loading thread should finish all by itself.
 */

/**
 * A read-through cache. This cache does not care about memory, it only cares about time. Entries expire and are
 * reloaded. Old entries will be purged periodically.
 *
 * @author René de Bloois
 * @since 2012
 */
public class ReadThroughCache
{
	static final Logger ___ = LoggerFactory.getLogger( ReadThroughCache.class );

	/**
	 * Default cache expiration 10 minutes.
	 */
	static public final int DEFAULT_EXPIRATION_MILLIS = 600000;

	/**
	 * Grace period 2 minutes.
	 */
	static public final int DEFAULT_GRACE_PERIOD_MILLIS = 120000;

	/**
	 * Default load timeout 1 minute.
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
	 * Blocking mode.
	 */
	static public enum BlockingMode
	{
		/**
		 * All threads requesting a specific cache entry will block when that cache entry is being (re)loaded.
		 */
		ALL,
		/**
		 * During a reload, only the thread that caused the reload will block, the rest gets the old value. During an initial
		 * load, including an expired cache entry outside the grace period, all threads will block like
		 * {@link BlockingMode#ALL}.
		 */
		SINGLE,
		/**
		 * During a reload, no thread will block, all threads will receive the old value. During an initial
		 * load, including an expired cache entry outside the grace period, all threads will block like
		 * {@link BlockingMode#ALL}.
		 */
		NONE
	}

	/**
	 * The default global cache.
	 */
	static private final ReadThroughCache defaultCache = new ReadThroughCache();

	/**
	 * The cache.
	 */
	private final Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

	/**
	 * Current expiration interval.
	 */
	private volatile int expirationMillis = DEFAULT_EXPIRATION_MILLIS;

	/**
	 * Current grace period.
	 */
	private volatile int gracePeriodMillis = DEFAULT_GRACE_PERIOD_MILLIS;

	/**
	 * Current load timeout.
	 */
	private volatile int loadTimeoutMillis = DEFAULT_LOAD_TIMEOUT_MILLIS;

	/**
	 * Current wait timeout.
	 */
	private volatile int waitTimeoutMillis = DEFAULT_WAIT_TIMEOUT_MILLIS;

	/**
	 * Current purge interval.
	 */
	private volatile int purgeIntervalMillis = DEFAULT_PURGE_INTERVAL_MILLIS;

	/**
	 * Current purge age.
	 */
	private volatile int purgeAgeMillis = DEFAULT_PURGE_AGE_MILLIS;

	/**
	 * The next purge moment.
	 */
	private volatile long nextPurgeMillis = 0;

	/**
	 * Lock object.
	 */
	private final Object nextPurgeMillisLock = new Object();

	/**
	 * Blocking mode.
	 */
	// TODO Ability to overrule blocking/nonblocking
	private volatile BlockingMode blockingMode = BlockingMode.ALL;


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
	 * Sets the number of milliseconds before a cache entry expires. On the next access the entry will be reloaded.
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
	 * Returns the grace period.
	 *
	 * @return The grace period.
	 */
	public int getGracePeriodMillis()
	{
		return this.gracePeriodMillis;
	}

	/**
	 * Sets the grace period. The grace period only has effect for non blocking access or when the cache is in non blocking mode.
	 *
	 * @param gracePeriodMillis The grace period.
	 */
	public void setGracePeriodMillis( int gracePeriodMillis )
	{
		this.gracePeriodMillis = gracePeriodMillis;
	}

	/**
	 * Returns the load timeout.
	 *
	 * @return The load timeout.
	 */
	public int getLoadTimeoutMillis()
	{
		return this.loadTimeoutMillis;
	}

	/**
	 * Sets the load timeout.
	 *
	 * @param loadTimeoutMillis The load timeout.
	 */
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
	public int getPurgeIntervalMillis()
	{
		return this.purgeIntervalMillis;
	}

	/**
	 * Sets the purge period, which defines the purge frequency.
	 *
	 * @param purgeInterval The purge period, which defines the purge frequency.
	 */
	public void setPurgeIntervalMillis( int purgeInterval )
	{
		if( purgeInterval < 0 )
			throw new IllegalArgumentException( "purgeInterval can't be negative" );
		this.purgeIntervalMillis = purgeInterval;
		synchronized( this.nextPurgeMillisLock )
		{
			this.nextPurgeMillis = 0;
		}
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
	 * Returns the blocking mode.
	 *
	 * @return The blocking mode.
	 */
	public BlockingMode getBlockingMode()
	{
		return this.blockingMode;
	}

	/**
	 * Sets the blocking mode.
	 *
	 * @param blockingMode The blocking mode.
	 */
	public void setBlockingMode( BlockingMode blockingMode )
	{
		this.blockingMode = blockingMode;
	}

	// END ------ Getters & Setters



	// ! No logging inside synchronized blocks !

	// TODO get with blocking mode override
	// TODO get without loader: 1 loader is configured and receives the key to determine what it should load
	// TODO named parameters, key becomes: par1:dkjhdkjh;par2:dkjhdkjh, pars sorted

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
		Loading loading = null;
		Failed failed = null;

		long now;

		// Purge
		boolean purge = false;
		synchronized( this.nextPurgeMillisLock )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block
			if( now >= this.nextPurgeMillis ) // A long is not thread safe!
			{
				this.nextPurgeMillis = now + this.purgeIntervalMillis;
				purge = true;
			}
		}
		if( purge )
			purge(); // TODO Separate thread? Depends on the speed, should not be needed.

		// Read the cache, check for expiration and control (re)loading
		String __ = null;
		synchronized( this.cache )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block

			result = this.cache.get( keyString );

			if( result == null )
			{
				loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
				this.cache.put( keyString, loading );
				__ = "miss [{}]";
			}
			else if( now >= result.getExpirationTime() )
			{
				if( result instanceof Loaded )
				{
					if( now < result.getExpirationTime() + this.gracePeriodMillis )
					{
						loading = new Reloading( ( (Loaded)result ).getValue(), now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
						__ = "expired grace [{}]";
					}
					else
					{
						loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
						__ = "expired [{}]";
					}
					this.cache.put( keyString, loading );
				}
				else if( result instanceof Failed )
				{
					loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
					this.cache.put( keyString, loading );
					__ = "fail expired [{}]";
				}
				else
				{
					Exception e = new IllegalStateException( "LoadingCacheEntry expired in cache" );
					failed = new Failed( e, now, now + this.expirationMillis );
					this.cache.put( keyString, failed );
				}
			}
		}

		if( failed != null )
		{
			// Want to do this outside the synchronized block
			( (Loading)result ).setResult( failed ); // Notifies all waiting threads
			result = failed;
			__ = "load expired [{}]";
		}

		if( __ != null )
			___.debug( __, keyString );

		// 1. result contains a CacheEntry or null
		// 2. loading contains an entry we need to load and overrules result

		if( loading != null )
		{
			if( this.blockingMode == BlockingMode.NONE && loading instanceof Reloading )
			{
				Reloading reloading = (Reloading)loading;
				___.debug( "background load [" + keyString + "]" );
				backgroundLoad( reloading, keyString, loader );
				___.debug( "use old [" + keyString + "]" );
				return (T)reloading.getOldValue();
			}

			___.debug( "blocking load [" + keyString + "]" );
			return load( loading, keyString, loader );
		}

		// 1. result contains a CacheEntry

		if( result instanceof Loading )
		{
			if( this.blockingMode != BlockingMode.ALL && result instanceof Reloading )
			{
				___.debug( "use old [" + keyString + "]" );
				return (T)( (Reloading)result ).getOldValue();
			}
			___.debug( "waiting [" + keyString + "]" );
			result = ( (Loading)result ).getResult( keyString ); // Blocking
			___.debug( "ready [" + keyString + "]" );
		}

		// 1. result contains a Loaded or a Failed

		if( result instanceof Loaded )
		{
			___.debug( "hit [" + keyString + "]" );
			return (T)( (Loaded)result ).getValue();
		}

		// Wrap because we wouldn't want to rethrow ThreadDeath or ThreadInterrupted, which would cause this thread to kill itself too
		throw new CacheException( "Previous load failed", ( (Failed)result ).getThrowable() );
	}

	private void replace( String keyString, CacheEntry original, CacheEntry replacement )
	{
		synchronized( this.cache )
		{
			CacheEntry e = this.cache.get( keyString );
			if( e != original )
				return;
			this.cache.put( keyString, replacement );
		}
	}

	/**
	 * Call the loader and put the result in the cache.
	 *
	 * @param loading The entry to load.
	 * @param key The key of the entry to load.
	 * @param loader The loader to use.
	 * @return The value loaded.
	 */
	<T> T load( Loading loading, String keyString, Loader<T> loader )
	{
		T value;

		try
		{
			value = loader.load();
		}
		catch( Throwable throwable )
		{
			long now = System.currentTimeMillis();
			CacheEntry result = new Failed( throwable, now, now + this.expirationMillis );
			replace( keyString, loading, result ); // Put it in the cache first for all to find
			loading.setResult( result ); // Notifies all waiting threads

			___.debug( "load failed [" + keyString + "]" );

			if( throwable instanceof Error )
				throw (Error)throwable;
			if( throwable instanceof RuntimeException )
				throw (RuntimeException)throwable;
			throw new CacheException( "Unexpected checked exception", throwable );
		}

		long now = System.currentTimeMillis();
		CacheEntry result = new Loaded( value, now, now + this.expirationMillis );
		replace( keyString, loading, result ); // Put it in the cache first for all to find
		loading.setResult( result ); // Notifies all waiting threads

		___.debug( "load success [" + keyString + "]" );

		return value;
	}

	private void backgroundLoad( final Loading loading, final String keyString, final Loader<?> loader )
	{
		new Thread() // TODO Should this thread be managed?
		{
			@Override
			public void run()
			{
				try
				{
					load( loading, keyString, loader );
				}
				catch( Throwable t )
				{
					// TODO Do not log ThreadDeath and ThreadInterrupted. Actually ThreadInterrupted will not be thrown here.
					___.error( "", t );
				}
			}
		}.start();
	}

	/**
	 * Purges entries from the cache which have aged a lot.
	 *
	 * @param now The now.
	 */
	// TODO What about a collector that runs more frequent to collect load timeouts?
	// TODO And what about a separate thread for the collector? But we do not want 1000 threads when someone creates 1000 different caches. So we need a cache manager.
	private void purge()
	{
		___.debug( "Purging..." );

		long now = System.currentTimeMillis();
		long then = now - this.purgeAgeMillis;
		List<Map.Entry<String, CacheEntry>> purged = new ArrayList<Map.Entry<String, CacheEntry>>();

		synchronized( this.cache )
		{
			for( Iterator<Map.Entry<String, CacheEntry>> iter = this.cache.entrySet().iterator(); iter.hasNext(); )
			{
				Entry<String, CacheEntry> entry = iter.next();
				CacheEntry e = entry.getValue();
				if( e.getStoredTime() < then )
				{
					iter.remove();
					purged.add( entry );
				}
			}
		}

		for( Entry<String, CacheEntry> entry : purged )
			if( entry.getValue() instanceof Loading )
			{
				___.warn( "purged loading [{}]", entry.getKey() );
				Exception ex = new IllegalStateException( "LoadingCacheEntry purged from cache [" + entry.getKey() + "]" );
				CacheEntry failed = new Failed( ex, now, now + this.expirationMillis );
				( (Loading)entry.getValue() ).setResult( failed );
			}
			else
				___.debug( "purged [{}]", entry.getKey() );
	}

	/**
	 * Transform an array of objects to a key.
	 *
	 * @param objects The array of objects.
	 * @return The resulting key.
	 */
	// TODO Use the Key class
	static public String buildKey( Object... objects )
	{
		StringBuilder result = new StringBuilder( 32 );
		for( Object object : objects )
		{
			if( result.length() > 0 )
				result.append( ';' );
			if( object == null )
				result.append( "*" );
			else
			{
				char[] chars = object.toString().toCharArray();
				int len = chars.length;
				for( int i = 0; i < len; )
				{
					char c = chars[ i++ ];
					switch( c )
					{
						case '*':
							if( len == 1 )
								result.append( '\\' );
							result.append( c );
							break;
						case '\\':
						case ';':
							result.append( '\\' ); //$FALL-THROUGH$
						default:
							result.append( c );
					}
				}
			}
		}
		return result.toString();
	}


	// ---------- Here are the cache entry types

	/**
	 * Cache entry storing the store time and expiration time.
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

	/**
	 * Cache entry representing a load in progress. Has one mutable field where the result is stored. All access to this field is synchronized.
	 */
	static private class Loading extends CacheEntry
	{
		private CacheEntry result;
		private int waitTimeoutMillis;

		Loading( long stored, long expiration, int waitTimeoutMillis )
		{
			super( stored, expiration );
			this.waitTimeoutMillis = waitTimeoutMillis;
		}

		/**
		 * Returns the result. When the result is not available yet, the thread waits until it becomes available.
		 *
		 * @param key The key of the entry, only used for logging.
		 * @return The result of the load. This could be a {@link Loaded} or a {@link Failed} cache entry.
		 */
		synchronized public CacheEntry getResult( String key ) // TODO Don't like this parameter
		{
			// Don't wait if loading has finished already, we won't get a notify then
			if( this.result != null )
				return this.result;

			try
			{
				if( this.waitTimeoutMillis <= 0 )
				{
					do
						wait();
					while( this.result == null ); // Guard against spurious wake ups
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
					while( this.result == null && now + 500 < stop ); // Guard against spurious wake ups + 1/2 second slack for inaccuracy
				}
			}
			catch( InterruptedException e )
			{
				throw new ThreadInterrupted();
			}

			if( this.result == null )
				throw new CacheTimeoutException( "Timed out waiting for the cache entry to be (re)loaded [" + key + "]" );

			return this.result;
		}

		synchronized public void setResult( CacheEntry result )
		{
			// TODO 2 messages, one if Failed already, and one if not Failed
			if( this.result != null )
				throw new IllegalStateException( "result is already filled, the (re)load probably took too long" );

			this.result = result;
			notifyAll();
		}
	}


	/**
	 * Cache entry representing a reload in progress. See {@link Loading}.
	 */
	static private class Reloading extends Loading
	{
		private Object oldValue;

		Reloading( Object oldValue, long stored, long expiration, int waitTimeoutMillis )
		{
			super( stored, expiration, waitTimeoutMillis );
			this.oldValue = oldValue;
		}

		/**
		 * Returns the old value stored in this cache entry. The value can be null.
		 *
		 * @return The old value stored in this cache entry.
		 */
		public Object getOldValue()
		{
			return this.oldValue;
		}
	}

	/**
	 * Represents a loaded cache entry. This is an immutable object.
	 */
	static private class Loaded extends CacheEntry
	{
		private Object value;

		Loaded( Object value, long stored, long expiration )
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

	/**
	 * Represents a cache entry that failed to load. This is an immutable object.
	 */
	static private class Failed extends CacheEntry
	{
		private Throwable throwable;

		Failed( Throwable throwable, long stored, long expiration )
		{
			super( stored, expiration );
			this.throwable = throwable;
		}

		public Throwable getThrowable()
		{
			return this.throwable;
		}
	}

	/**
	 * Interface used to load something.
	 *
	 * @param <T> The type of that which is going to be loaded.
	 */
	static public interface Loader<T>
	{
		/**
		 * Load something.
		 *
		 * @return That which has been loaded.
		 */
		T load();
	}
}
