package solidstack.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import solidstack.SystemException;
import solidstack.cache.ReadThroughCache.Loader;


@SuppressWarnings( "javadoc" )
public class CacheTests
{
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
		cache.setWaitTimeoutMillis( 10000 );

		final Random random = new Random();

		final Loader<String> loader = new Loader<String>()
		{
			public String load()
			{
				try
				{
					Thread.sleep( 500 + random.nextInt( 10 ) * 500 );
				}
				catch( InterruptedException e )
				{
					Thread.currentThread().interrupt();
				}
				if( random.nextInt( 20 ) == 0 )
					throw new RuntimeException( "load failed" );
				return null;
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
						while( !Thread.interrupted() )
						{
							this.myLifeSign.set( System.currentTimeMillis() );

							int val = random.nextInt( 10 );
							try
							{
								cache.get( loader, val );
							}
							catch( Exception e )
							{
								System.out.println( e.toString() );
							}
							sleep( random.nextInt( 10 ) * 10 );
						}
					}
					catch( InterruptedException e )
					{
						System.out.println( "Thread interrupted" );
					}
					catch( ThreadDeath e )
					{
						System.out.println( "Thread died" );
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
				System.out.println( "Threads alive: " + count );
			}
			System.out.println( "Killing threads" );
			for( Thread thread : threads )
				thread.interrupt();
			System.out.println( "Waiting for threads" );
			for( Thread thread : threads )
				thread.join();
		}
		catch( InterruptedException e )
		{
			throw new SystemException( e );
		}
	}
}
