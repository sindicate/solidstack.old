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

			Selector selector = Selector.open();
			server.register( selector, SelectionKey.OP_ACCEPT );

			while( true )
			{
				System.out.println( "Selecting from " + selector.keys().size() + " keys" );
				int selected = selector.select();
				System.out.println( "Selected " + selected + " keys" );

				Set< SelectionKey > keys = selector.selectedKeys();
				for( Iterator< SelectionKey > i = keys.iterator(); i.hasNext(); )
				{
					SelectionKey key = i.next(); // No need to synchronize on the key

					if( !key.isValid() )
						continue;

					if( key.isAcceptable() )
					{
						server = (ServerSocketChannel)key.channel();
						SocketChannel channel = server.accept();
						if( channel != null )
						{
							System.out.println( "Channel (" + DebugId.getId( channel ) + ") New channel" );

							channel.configureBlocking( false );
							key = channel.register( selector, SelectionKey.OP_READ );
							Handler handler = new Handler( channel, key, this.application );
							key.attach( handler );

							System.out.println( "Channel (" + DebugId.getId( channel ) + ") atached handler" );
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

						( (Handler)key.attachment() ).dataReady();
					}

					if( key.isWritable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Writable" );

						synchronized( key )
						{
							key.interestOps( key.interestOps() ^ SelectionKey.OP_WRITE );
						}

						( (Handler)key.attachment() ).writeReady();
					}

					if( key.isConnectable() )
					{
						final SocketChannel channel = (SocketChannel)key.channel();
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") Connectable" );
					}
				}

				keys.clear();
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}
}
