package solidstack.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	// TODO Build the timeouts on the keys?
	private Map<ReadListener, Timeout> timeouts = new HashMap<ReadListener, Timeout>(); // TODO Use DelayQueue or other form of concurrent datastructure
	private long nextTimeout;

	public Dispatcher() throws IOException
	{
		this.selector = Selector.open();
	}

	public void execute( Runnable command )
	{
		this.executor.execute( command );
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

	public AsyncSocketChannelHandler connectAsync( String hostname, int port ) throws IOException
	{
		return (AsyncSocketChannelHandler)connect( hostname, port, new AsyncSocketChannelHandler( this ) );
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

	public void addTimeout( ReadListener listener, int timeout )
	{
		synchronized( this.timeouts )
		{
			this.timeouts.put( listener, new Timeout( listener, System.currentTimeMillis() + timeout ) );
		}
	}

	public void removeTimeout( ReadListener listener )
	{
		synchronized( this.timeouts )
		{
			this.timeouts.remove( listener );
		}
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
				int selected = this.selector.select( 10000 );
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

								ServerSocketChannelHandler handler = new ServerSocketChannelHandler( this, key );
								handler.setListener( listener );
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

				long now = System.currentTimeMillis();

				if( Loggers.nio.isDebugEnabled() )
					if( now >= this.next )
					{
						Loggers.nio.debug( "Active count/keys: {}/{}", this.executor.getActiveCount(), this.selector.keys().size() );
						this.next = now + 1000;
					}

				if( now >= this.nextTimeout )
				{
					this.nextTimeout = now + 10000;

					Loggers.nio.trace( "Processing timeouts" );

					List<Timeout> timedouts = new ArrayList<Timeout>();
					synchronized( this.timeouts )
					{
						for( Iterator<Timeout> i = this.timeouts.values().iterator(); i.hasNext(); )
						{
							Timeout timeout = i.next();
							if( timeout.getTimeout() <= now )
							{
								timedouts.add( timeout );
								i.remove();
							}
						}
					}

					for( Timeout timeout : timedouts )
						timeout.getListener().timeout();
				}
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
