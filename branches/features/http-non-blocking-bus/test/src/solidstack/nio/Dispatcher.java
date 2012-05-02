package solidstack.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
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
	private long next;
//	static final public boolean debug = true;

	public Dispatcher() throws IOException
	{
		this.selector = Selector.open();
	}

	public void execute( Runnable command )
	{
		this.executor.execute( command );
		if( Loggers.nio.isDebugEnabled() )
		{
			long now = System.currentTimeMillis();
			if( now >= this.next )
			{
				Loggers.nio.debug( "Active count: {}", this.executor.getActiveCount() );
				this.next = now + 1000;
			}
		}
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
		if( Loggers.nio.isTraceEnabled() )
			Loggers.nio.trace( "Channel ({}) Waiting for read", DebugId.getId( key.channel() ) );

		synchronized( key )
		{
			// TODO Only set and wakeup when not set already
			key.interestOps( key.interestOps() | SelectionKey.OP_READ );
		}
		key.selector().wakeup();
	}

	public void listenWrite( SelectionKey key )
	{
		if( Loggers.nio.isTraceEnabled() )
			Loggers.nio.trace( "Channel ({}) Waiting for write", DebugId.getId( key.channel() ) );

		synchronized( key )
		{
			// TODO Only set and wakeup when not set already
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
		Loggers.nio.info( "Shutting down dispatcher" );
		this.executor.shutdown();
		if( this.executor.awaitTermination( 1, TimeUnit.HOURS ) )
			Loggers.nio.info( "Thread pool shut down, interrupting dispatcher thread" );
		else
		{
			Loggers.nio.info( "Thread pool not shut down, interrupting" );
			this.executor.shutdownNow();
			if( !this.executor.awaitTermination( 1, TimeUnit.HOURS ) )
				Loggers.nio.info( "Thread pool could not be shut down" );
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

				if( Loggers.nio.isTraceEnabled() )
					Loggers.nio.trace( "Selecting from {} keys", this.selector.keys().size() );
				int selected = this.selector.select();
				Loggers.nio.trace( "Selected {} keys", selected );

				Set< SelectionKey > keys = this.selector.selectedKeys();
				for( Iterator< SelectionKey > i = keys.iterator(); i.hasNext(); )
				{
					SelectionKey key = i.next(); // No need to synchronize on the key

					try
					{
						if( !key.isValid() )
							continue;

						if( key.isAcceptable() )
						{
							ServerSocketChannel server = (ServerSocketChannel)key.channel();
							SocketChannel channel = server.accept();
							if( channel != null )
							{
								if( Loggers.nio.isTraceEnabled() )
									Loggers.nio.trace( "Channel ({}) New channel", DebugId.getId( channel ) );

								ReadListener listener = (ReadListener)key.attachment();

								channel.configureBlocking( false );
								key = channel.register( this.selector, SelectionKey.OP_READ );

								SocketChannelHandler handler = new ServerSocketChannelHandler( this, key, listener );
								key.attach( handler );
							}
							else
								if( Loggers.nio.isTraceEnabled() )
									Loggers.nio.trace( "Lost accept" );
						}

						if( key.isReadable() )
						{
							// TODO Detect close gives -1 on the read

							final SocketChannel channel = (SocketChannel)key.channel();
							if( Loggers.nio.isTraceEnabled() )
								Loggers.nio.trace( "Channel ({}) Readable", DebugId.getId( channel ) );

							synchronized( key )
							{
								key.interestOps( key.interestOps() ^ SelectionKey.OP_READ );
							}

							SocketChannelHandler handler = (SocketChannelHandler)key.attachment();
							handler.dataIsReady();
						}

						if( key.isWritable() )
						{
							final SocketChannel channel = (SocketChannel)key.channel();
							if( Loggers.nio.isTraceEnabled() )
								Loggers.nio.trace( "Channel ({}) Writable", DebugId.getId( channel ) );

							synchronized( key )
							{
								key.interestOps( key.interestOps() ^ SelectionKey.OP_WRITE );
							}

							SocketChannelHandler handler = (SocketChannelHandler)key.attachment();
							handler.writeIsReady();
						}

						if( key.isConnectable() )
						{
							final SocketChannel channel = (SocketChannel)key.channel();
							if( Loggers.nio.isTraceEnabled() )
								Loggers.nio.trace( "Channel ({}) Connectable", DebugId.getId( channel ) );

							Assert.fail( "Shouldn't come here" );
						}
					}
					catch( CancelledKeyException e )
					{
						// Ignore
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
			Loggers.nio.info( "Dispatcher ended" );
		else
		{
			Loggers.nio.info( "Dispatcher ended, shutting down thread pool" );
			try
			{
				shutdownThreadPool();
			}
			catch( InterruptedException e )
			{
				throw new SystemException( e );
			}
			Loggers.nio.info( "Thread pool shut down" );
		}
	}
}
