package solidstack.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import solidstack.cache.ReadThroughCache.BlockingMode;
import solidstack.cache.ReadThroughCache.Loader;
import solidstack.lang.SystemException;
import solidstack.lang.ThreadInterrupted;


@SuppressWarnings( "javadoc" )
public class CacheTests
{
	static final Logger log = LoggerFactory.getLogger( CacheTests.class );

	@Test( groups = "new" )
	public void test1()
	{
		SimpleCache cache = new SimpleCache();
		for( int i = 0; i < 100; i++ )
			cache.put( i, i, i, i );

		int i = cache.get( 1, 1, 1 );
		System.out.println( i );
	}

	@Test( groups = "new" )
	public void test2()
	{
		final ReadThroughCache cache = new ReadThroughCache();
		cache.setExpirationMillis( 2000 );
		cache.setGracePeriodMillis( 1000 );

		cache.setBlockingMode( BlockingMode.NONE );
		cache.setWaitTimeoutMillis( 10000 );

		cache.setLoadTimeoutMillis( 5000 );
		cache.setPurgeIntervalMillis( 10000 );
		cache.setPurgeAgeMillis( 0 );

		final Random random = new Random();

		final Loader<String> loader = new Loader<String>()
		{
			public String load()
			{
				try
				{
					Thread.sleep( 500 + random.nextInt( 10 ) * 500 );
					int event = random.nextInt( 20 );
					if( event == 0 )
						throw new RuntimeException( "load failed" );
					if( event == 1 )
						Thread.sleep( 5000 );
					return null;
				}
				catch( InterruptedException e )
				{
					throw new ThreadInterrupted( e );
				}
			}
		};

		List<AtomicLong> lifeSigns = new ArrayList<AtomicLong>();
		List<Thread> threads = new ArrayList<Thread>();
		for( int i = 0; i < 40; i++ )
		{
			final AtomicLong lifeSign = new AtomicLong();
			lifeSigns.add( lifeSign );
			Thread thread = new Thread()
			{
				public AtomicLong myLifeSign = lifeSign;

				@Override
				public void run()
				{
					try
					{
						while( true )
						{
							this.myLifeSign.set( System.currentTimeMillis() );

							int val = random.nextInt( 10 );
							try
							{
								cache.get( loader, val );
							}
							catch( Exception e )
							{
								log.error( "", e );
							}
							sleep( random.nextInt( 10 ) * 10 );
						}
					}
					catch( InterruptedException e )
					{
						log.debug( "thread ended" );
					}
					catch( ThreadInterrupted e )
					{
						log.debug( "thread ended" );
					}
					catch( ThreadDeath e )
					{
						log.debug( "thread died" );
					}
				}
			};
			thread.start();
			threads.add( thread );
		}

		try
		{
			for( int i = 0; i < 10; i++ )
			{
				Thread.sleep( 10000 );
				long now = System.currentTimeMillis();
				int count = 0;
				for( AtomicLong atomicLong : lifeSigns )
				{
					if( atomicLong.get() >= now - 10000 )
						count++;
				}
				log.debug( "Threads alive: {}", count );
			}

			log.info( "Interrupting threads..." );
			for( Thread thread : threads )
			{
				final StackTraceElement[] stackTrace = thread.getStackTrace();
				thread.interrupt();
				Throwable t = new Throwable( "Stacktrace dump" )
				{
					@Override
					public synchronized Throwable fillInStackTrace()
					{
						setStackTrace( stackTrace );
						return this;
					}
				};
				log.debug( "interrupting [{}]", thread, t );
			}

			log.info( "Waiting for threads..." );
			long now = System.currentTimeMillis();
			long stop = now + 20000;
			for( Iterator<Thread> i = threads.iterator(); i.hasNext(); )
			{
				Thread thread = i.next();
				thread.join( stop - now );
				if( thread.isAlive() )
					break;
				i.remove();
				now = System.currentTimeMillis();
			}

			if( !threads.isEmpty() )
			{
				log.info( "Stopping remaining threads..." );
				for( Thread thread : threads )
					thread.stop();
			}
		}
		catch( InterruptedException e )
		{
			throw new SystemException( e );
		}
	}
}
