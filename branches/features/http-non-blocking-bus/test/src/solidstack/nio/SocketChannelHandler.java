package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import solidstack.httpserver.ApplicationContext;
import solidstack.io.FatalIOException;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
abstract public class SocketChannelHandler implements Runnable
{
	private Dispatcher dispatcher;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;
	private AtomicBoolean running = new AtomicBoolean();

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public SocketChannelHandler( Dispatcher dispatcher, SelectionKey key )
	{
		this.dispatcher = dispatcher;
		this.key = key;

		this.in = new SocketChannelInputStream( this );
		this.out = new SocketChannelOutputStream( this );
	}

	public SocketChannelInputStream getInputStream()
	{
		return this.in;
	}

	public SocketChannelOutputStream getOutputStream()
	{
		return this.out;
	}

	public Dispatcher getDispatcher()
	{
		return this.dispatcher;
	}

	public SocketChannel getChannel()
	{
		return (SocketChannel)this.key.channel();
	}

	public SelectionKey getKey()
	{
		return this.key;
	}

	public boolean isRunningAndSet()
	{
		return !this.running.compareAndSet( false, true );
	}

	abstract public void incoming() throws IOException;

	public void run()
	{
		SocketChannel channel = getChannel();
		SelectionKey key = getKey();

		boolean complete = false;
		try
		{
			try
			{
				while( true )
				{
					incoming();

					if( channel.isOpen() )
					{
						if( getInputStream().available() == 0 )
						{
							getDispatcher().read( key );
							complete = true;
							return;
						}
					}
					else
					{
						complete = true;
						return;
					}
				}
			}
			finally
			{
				if( !complete )
				{
					channel.close();
					System.out.println( "Channel (" + DebugId.getId( channel ) + ") thread aborted" );
				}
				else
					System.out.println( "Channel (" + DebugId.getId( channel ) + ") thread complete" );

				this.running.set( false );
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	public void dataIsReady()
	{
		synchronized( this.in )
		{
			this.in.notify();
		}
	}

	public void writeIsReady()
	{
		synchronized( this.out )
		{
			this.out.notify();
		}
	}
}
