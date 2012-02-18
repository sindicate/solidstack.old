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
import solidstack.lang.ThreadInterrupted;


/**
 * A read-through cache. This cache does not care about memory, it only cares about time. Entries expire and are
 * reloaded. Old entries will be purged periodically.
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

	static public enum BlockingMode { ALL, SINGLE, NONE }

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
	private int purgeIntervalMillis = DEFAULT_PURGE_INTERVAL_MILLIS;

	/**
	 * Current purge age.
	 */
	private int purgeAgeMillis = DEFAULT_PURGE_AGE_MILLIS;

	/**
	 * The next purge moment.
	 */
	private long nextPurgeMillis = 0;

	// TODO Ability to overrule blocking/nonblocking
	private BlockingMode blockingMode = BlockingMode.ALL;


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

	public int getLoadTimeoutMillis()
	{
		return this.loadTimeoutMillis;
	}

	/**
	 * Sets the load timeout.
	 *
	 * @param loadTimeoutMillis
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
		this.nextPurgeMillis = 0;
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

	public BlockingMode getBlockingMode()
	{
		return this.blockingMode;
	}


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
		CacheEntry loading = null;

		long now;

		// Purge
		boolean purge = false;
		synchronized( this )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block
			if( now >= this.nextPurgeMillis ) // A long is not thread safe!
			{
				this.nextPurgeMillis = now + this.purgeIntervalMillis;
				purge = true;
			}
		}
		if( purge )
			purge( now ); // TODO Separate thread? Depends on the speed, should not be needed.

		// Read the cache, check for expiration and control (re)loading
		String logmessage = null;
		synchronized( this.cache )
		{
			now = System.currentTimeMillis(); // Get the time after entering the synchronized block

			result = this.cache.get( keyString );

			if( result == null )
			{
				loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
				this.cache.put( keyString, loading );
				logmessage = "miss [{}]";
			}
			else if( now >= result.getExpirationTime() )
			{
				if( result instanceof Loaded )
				{
					if( now < result.getExpirationTime() + this.gracePeriodMillis )
					{
						loading = new Reloading( ( (Loaded)result ).getValue(), now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
						logmessage = "expired grace [{}]";
					}
					else
					{
						loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
						logmessage = "expired [{}]";
					}
					this.cache.put( keyString, loading );
				}
				else if( result instanceof Failed )
				{
					loading = new Loading( now, now + this.loadTimeoutMillis, this.waitTimeoutMillis );
					this.cache.put( keyString, loading );
					logmessage = "fail expired [{}]";
				}
				else
				{
					Exception e = new IllegalStateException( "LoadingCacheEntry expired in cache" );
					CacheEntry failed = new Failed( e, now, now + this.expirationMillis );
					this.cache.put( keyString, failed );
					( (Loading)result ).setResult( failed );
					result = failed;
					logmessage = "load expired [{}]";
				}
			}
		}

		if( logmessage != null )
			log.debug( logmessage, keyString );

		// 1. result contains a CacheEntry or null
		// 2. loading contains an entry we need to load and overrules result

		if( loading != null )
		{
			if( this.blockingMode == BlockingMode.NONE && loading instanceof Reloading )
			{
				log.debug( "background load [" + keyString + "]" );
				final Reloading _loading = (Reloading)loading;
				// TODO Should this thread be managed?
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							load( _loading, keyString, loader );
						}
						catch( Throwable t )
						{
							log.error( "", t ); // TODO Should we not log ThreadDeath? Of should we implement an UncaughtExceptionHandler?
						}
					}
				}.start();
				log.debug( "use old [" + keyString + "]" );
				return (T)_loading.getOldValue();
			}

			log.debug( "blocking load [" + keyString + "]" );
			return load( (Loading)loading, keyString, loader );
		}

		// 1. result contains a CacheEntry

		if( result instanceof Loading )
		{
			if( this.blockingMode != BlockingMode.ALL && result instanceof Reloading )
			{
				log.debug( "use old [" + keyString + "]" );
				return (T)( (Reloading)result ).getOldValue();
			}
			log.debug( "waiting [" + keyString + "]" );
			try
			{
				result = ( (Loading)result ).getResult( keyString ); // Blocking
			}
			catch( InterruptedException e )
			{
				throw new ThreadInterrupted( e ); // TODO The advantage of ThreadDeath is that it does not get printed to System.err
			}
			log.debug( "ready [" + keyString + "]" );
		}

		// 1. result contains a Loaded or a Failed

		if( result instanceof Loaded )
		{
			log.debug( "hit [" + keyString + "]" );
			return (T)( (Loaded)result ).getValue();
		}

		Exception e = ( (Failed)result ).getException();
		if( e instanceof RuntimeException )
			throw (RuntimeException)e;
		throw new SystemException( e );
	}

	/**
	 * Call the loader and put the result in the cache.
	 *
	 * @param entry The entry to load.
	 * @param key The key of the entry to load.
	 * @param loader The loader to use.
	 * @return The value loaded.
	 */
	<T> T load( Loading entry, String keyString, Loader<T> loader )
	{
		T value = null;
		Exception exception = null;

		try
		{
			value = loader.load();
			log.debug( "load success [" + keyString + "]" );
		}
		catch( Exception t )
		{
			exception = t;
			log.debug( "load failed [" + keyString + "]" );
		}

		long now = System.currentTimeMillis();
		CacheEntry result;
		if( exception != null )
			result = new Failed( exception, now, now + this.expirationMillis );
		else
			result = new Loaded( value, now, now + this.expirationMillis );

		synchronized( this.cache )
		{
			this.cache.put( keyString, result );
		}

		entry.setResult( result ); // Notifies all waiting threads

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
		boolean warn = log.isWarnEnabled();
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
					if( e instanceof Loading )
					{
						Exception ex = new IllegalStateException( "LoadingCacheEntry purged from cache [" + entry.getKey() + "]" );
						CacheEntry failed = new Failed( ex, now, now + this.expirationMillis );
						( (Loading)e ).setResult( failed );

						if( warn )
							purged.add( entry );
					}
					else
						if( debug )
							purged.add( entry );
				}
			}
		}

		for( Entry<String, CacheEntry> entry : purged )
		{
			if( entry.getValue() instanceof Loading )
				log.warn( "purged loading [" + entry.getKey() + "]" );
			else
				log.debug( "purged [" + entry.getKey() + "]" );
		}
	}

	/**
	 * Transform an array of objects to a key.
	 *
	 * @param objects The array of objects.
	 * @return The resulting key.
	 */
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

	static private class Loading extends CacheEntry
	{
		private CacheEntry result;
		private int waitTimeoutMillis;

		Loading( long stored, long expiration, int waitTimeoutMillis )
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

			if( this.result == null )
				throw new CacheTimeoutException( "Timed out waiting for the cache entry to be (re)loaded [" + key + "]" );

			return this.result;
		}

		synchronized public void setResult( CacheEntry result )
		{
			if( this.result != null )
				throw new IllegalStateException( "result is already filled, the (re)load probably took too long" );

			this.result = result;
			notifyAll();
		}
	}

	static private class Reloading extends Loading
	{
		private Object oldValue;

		Reloading( Object oldValue, long stored, long expiration, int waitTimeoutMillis )
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

	static private class Failed extends CacheEntry
	{
		private Exception exception;

		Failed( Exception exception, long stored, long expiration )
		{
			super( stored, expiration );
			this.exception = exception;
		}

		public Exception getException()
		{
			return this.exception;
		}
	}

	static public interface Loader<T>
	{
		T load();
	}
}
