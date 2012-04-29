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
abstract public class AsyncSocketChannelHandler extends SocketChannelHandler implements Runnable
{
	private AtomicBoolean running = new AtomicBoolean();

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public AsyncSocketChannelHandler( Dispatcher dispatcher, SelectionKey key )
	{
		super( dispatcher, key );
	}

	protected boolean isRunningAndSet()
	{
		return !this.running.compareAndSet( false, true );
	}

	protected void endOfRunning()
	{
		this.running.set( false );
	}

	abstract protected void incoming() throws IOException;

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
}
