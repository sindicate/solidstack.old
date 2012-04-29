package solidstack.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import solidstack.lang.SystemException;


public class Dispatcher extends Thread
{
//	private List<ServerSocketChannelHandler> queue = new ArrayList();
	private Selector selector;
	private Object lock = new Object();

	public Dispatcher() throws IOException
	{
		this.selector = Selector.open();
	}

	public void listen( int port, ServerSocketChannelHandler handler ) throws IOException
	{
		ServerSocketChannel server = ServerSocketChannel.open();
		server.configureBlocking( false );
		server.socket().bind( new InetSocketAddress( port ) );

		SelectionKey key = server.register( this.selector, SelectionKey.OP_ACCEPT, handler );

		handler.setServer( server );
		handler.setKey( key );
	}

	public void read( SelectionKey key )
	{
		System.out.println( "Channel (" + DebugId.getId( key.channel() ) + ") Waiting for data" );
		synchronized( key )
		{
			key.interestOps( key.interestOps() | SelectionKey.OP_READ );
		}
		key.selector().wakeup();
	}

	public void write( SelectionKey key )
	{
		System.out.println( "Channel (" + DebugId.getId( key.channel() ) + ") Waiting for write" );
		synchronized( key )
		{
			key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
		}
		key.selector().wakeup();
	}

	public SocketChannelHandler connect( String hostname, int port ) throws IOException
	{
		SocketChannel channel = SocketChannel.open( new InetSocketAddress( hostname, port ) );
		channel.configureBlocking( false );
		SelectionKey key;
		synchronized( this.lock ) // Prevent register from blocking again
		{
			this.selector.wakeup();
			key = channel.register( this.selector, 0 );
		}
		SocketChannelHandler handler = new ClientSocketChannelHandler( this, key );
		key.attach( handler );
		return handler;
	}

	@Override
	public void run()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		try
		{
			while( !Thread.interrupted() )
			{
				synchronized( this.lock )
				{
					// Wait till the connect has done its registration TODO Is this the best way?
				}

				System.out.println( "Selecting from " + this.selector.keys().size() + " keys" );
				int selected = this.selector.select();
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
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") New channel" );

							ServerSocketChannelHandler handler = (ServerSocketChannelHandler)key.attachment();

							channel.configureBlocking( false );
							key = channel.register( this.selector, SelectionKey.OP_READ );

							SocketChannelHandler handler2 = handler.incoming( this, key );
							key.attach( handler2 );

							System.out.println( "Channel (" + DebugId.getId( channel ) + ") attached handler" );
						}
						else
							System.out.println( "Lost accept" );
					}

					if( key.isReadable() )
					{
						// TODO Detect close gives -1 on the read

						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Readable" );

						synchronized( key )
						{
							key.interestOps( key.interestOps() ^ SelectionKey.OP_READ );
						}

						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Data ready, notify" );
						SocketChannelHandler handler = (SocketChannelHandler)key.attachment();
						if( !handler.isRunningAndSet() )
							executor.execute( handler ); // TODO Also for write
						handler.dataIsReady();
					}

					if( key.isWritable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Writable" );

						synchronized( key )
						{
							key.interestOps( key.interestOps() ^ SelectionKey.OP_WRITE );
						}

						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Write ready, notify" );
						( (SocketChannelHandler)key.attachment() ).writeIsReady();
					}

					if( key.isConnectable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Connectable" );
					}
				}

//				for( ServerSocketChannelHandler handler : this.queue )
//				{
//					SelectionKey key = handler.getServer().register( this.selector, SelectionKey.OP_ACCEPT, handler );
//					handler.setKey( key );
//				}

				keys.clear();
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}

		System.out.println( "Dispatcher ended, shutting down thread pool" );
		executor.shutdownNow();
		System.out.println( "Thread pool shut down" );
	}
}
