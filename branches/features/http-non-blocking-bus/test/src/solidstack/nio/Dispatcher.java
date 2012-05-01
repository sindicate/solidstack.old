package solidstack.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import solidstack.lang.Assert;
import solidstack.lang.SystemException;


public class Dispatcher extends Thread
{
//	private List<ServerSocketChannelHandler> queue = new ArrayList();
	private Selector selector;
	private Object lock = new Object();
	private ThreadPoolExecutor executor;
	static final public boolean debug = true;

	public Dispatcher() throws IOException
	{
		this.selector = Selector.open();
	}

	public void listen( int port, ReadListener listener ) throws IOException
	{
		ServerSocketChannel server = ServerSocketChannel.open();
		server.configureBlocking( false );
		server.socket().bind( new InetSocketAddress( port ) ); // TODO Bind to specific network interface

		server.register( this.selector, SelectionKey.OP_ACCEPT, listener );
	}

	public void listenRead( SelectionKey key )
	{
		if( debug )
			System.out.println( "Channel (" + DebugId.getId( key.channel() ) + ") Waiting for data" );
		synchronized( key )
		{
			key.interestOps( key.interestOps() | SelectionKey.OP_READ );
		}
		key.selector().wakeup();
	}

	public void listenWrite( SelectionKey key )
	{
		if( debug )
			System.out.println( "Channel (" + DebugId.getId( key.channel() ) + ") Waiting for write" );
		synchronized( key )
		{
			key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
		}
		key.selector().wakeup();
	}

	public SocketChannelHandler connect( String hostname, int port ) throws IOException
	{
		return connect( hostname, port, new SocketChannelHandler( this ) );
	}

	public SocketChannelHandler connectAsync( String hostname, int port, ReadListener listener ) throws IOException
	{
		return connect( hostname, port, new AsyncSocketChannelHandler( this, listener ) );
	}

	private SocketChannelHandler connect( String hostname, int port, SocketChannelHandler handler ) throws IOException
	{
		SocketChannel channel = SocketChannel.open( new InetSocketAddress( hostname, port ) );
		channel.configureBlocking( false );
		SelectionKey key;
		synchronized( this.lock ) // Prevent register from blocking again
		{
			this.selector.wakeup();
			key = channel.register( this.selector, SelectionKey.OP_READ );
		}
		handler.setKey( key );
		key.attach( handler );
		return handler;
	}

	private void shutdownThreadPool() throws InterruptedException
	{
		System.out.println( "Shutting down dispatcher" );
		this.executor.shutdown();
		if( this.executor.awaitTermination( 1, TimeUnit.HOURS ) )
			System.out.println( "Thread pool shut down, interrupting dispatcher thread" );
		else
		{
			System.out.println( "Thread pool not shut down, interrupting" );
			this.executor.shutdownNow();
			if( !this.executor.awaitTermination( 1, TimeUnit.HOURS ) )
				System.out.println( "Thread pool could not be shut down" );
		}
	}

	public void shutdown() throws InterruptedException
	{
		shutdownThreadPool();
		interrupt();
		join();
	}

	@Override
	public void run()
	{
		long next = 0;

		this.executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
		try
		{
			while( !Thread.interrupted() )
			{
				synchronized( this.lock )
				{
					// Wait till the connect has done its registration TODO Is this the best way?
					// TODO Make sure this is not optimized away
				}

				if( debug )
					System.out.println( "Selecting from " + this.selector.keys().size() + " keys" );
				int selected = this.selector.select();
				if( debug )
					System.out.println( "Selected " + selected + " keys" );

				Set< SelectionKey > keys = this.selector.selectedKeys();
				for( Iterator< SelectionKey > i = keys.iterator(); i.hasNext(); )
				{
					SelectionKey key = i.next(); // No need to synchronize on the key

					if( !key.isValid() )
						continue;

					if( key.isAcceptable() )
					{
						ServerSocketChannel server = (ServerSocketChannel)key.channel();
						SocketChannel channel = server.accept();
						if( channel != null )
						{
							if( debug )
								System.out.println( "Channel (" + DebugId.getId( channel ) + ") New channel" );

							channel.configureBlocking( false );
							key = channel.register( this.selector, SelectionKey.OP_READ );

							SocketChannelHandler handler = new ServerSocketChannelHandler( this, key, (ReadListener)key.attachment() );
							key.attach( handler );
						}
						else
							if( debug )
								System.out.println( "Lost accept" );
					}

					if( key.isReadable() )
					{
						// TODO Detect close gives -1 on the read

						final SocketChannel channel = (SocketChannel)key.channel();
						if( debug )
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") Readable" );

						synchronized( key )
						{
							key.interestOps( key.interestOps() ^ SelectionKey.OP_READ );
						}

						if( debug )
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") Data ready, notify" );

						SocketChannelHandler handler = (SocketChannelHandler)key.attachment();
						if( handler instanceof AsyncSocketChannelHandler )
						{
							AsyncSocketChannelHandler h = (AsyncSocketChannelHandler)handler;
							if( !h.isRunningAndSet() )
							{
								this.executor.execute( h ); // TODO Also for write
								long now = System.currentTimeMillis();
								if( now >= next )
								{
									System.out.println( "Active count: " + this.executor.getActiveCount() );
									next = now + 1000;
								}
							}
						}
						handler.dataIsReady();
					}

					if( key.isWritable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						if( debug )
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") Writable" );

						synchronized( key )
						{
							key.interestOps( key.interestOps() ^ SelectionKey.OP_WRITE );
						}

						if( debug )
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") Write ready, notify" );

						SocketChannelHandler handler = (SocketChannelHandler)key.attachment();
						handler.writeIsReady();
					}

					if( key.isConnectable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						if( debug )
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") Connectable" );

						Assert.fail( "Shouldn't come here" );
					}
				}

				keys.clear();
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}

		if( this.executor.isTerminated() )
			System.out.println( "Dispatcher ended" );
		else
		{
			System.out.println( "Dispatcher ended, shutting down thread pool" );
			try
			{
				shutdownThreadPool();
			}
			catch( InterruptedException e )
			{
				throw new SystemException( e );
			}
			System.out.println( "Thread pool shut down" );
		}
	}
}
