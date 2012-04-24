package solidstack.httpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.HttpException;
import solidstack.lang.Assert;
import solidstack.lang.SystemException;


public class Server extends Thread
{
	private int port;
	private ApplicationContext application; // TODO Make this a Map
//	private Thread thread;

	public Server( int port )
	{
		this.port = port;
	}

	public void addApplication( ApplicationContext application )
	{
		this.application = application;
	}

	@Override
	public void run()
	{
		try
		{
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking( false );
			server.socket().bind( new InetSocketAddress( this.port ) );

			final Selector selector = Selector.open();
			server.register( selector, SelectionKey.OP_ACCEPT );

			while( true )
			{
				System.out.println( "Selector selecting from " + selector.keys().size() + " keys" );
				int selected = selector.select();
				Set< SelectionKey > keys = selector.selectedKeys();
				System.out.println( "Selector selected = " + selected + ": " + keys.size() );

				for( Iterator< SelectionKey > i = keys.iterator(); i.hasNext(); )
				{
					// TODO Synchronize on the key? Think not, see javadoc.
					SelectionKey key = i.next();
					i.remove();

					if( key.isAcceptable() )
					{
						server = (ServerSocketChannel)key.channel();
						SocketChannel channel = server.accept();
						Assert.notNull( channel );
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") New channel" );
						channel.configureBlocking( false );
						key = channel.register( selector, SelectionKey.OP_READ );
						Handler handler = new Handler( channel, key, this.application );
						key.attach( handler );
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") atached handler" );
					}
					else if( key.isReadable() )
					{
						// TODO Detect close gives -1 on the read
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Data ready, unregister" );
						key.interestOps( key.interestOps() ^ SelectionKey.OP_READ );
//						key.selector().wakeup();

						Handler handler = (Handler)key.attachment();
						handler.dataReady();
					}
					else if( key.isWritable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Write ready, unregister" );
						key.interestOps( key.interestOps() ^ SelectionKey.OP_WRITE );
//						key.selector().wakeup(); // TODO Why is this?

						Handler handler = (Handler)key.attachment();
						handler.writeReady();
					}
	//				else if( key.isConnectable() )
	//				{
	//					final SocketChannel channel = (SocketChannel)key.channel();
	//					System.out.println( "Channel (" + DebugId.getId( channel ) + ") Connection event" );
	//				}
					else
						throw new HttpException( "Unexpected ops: " + key.readyOps() );
				}
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}
}
