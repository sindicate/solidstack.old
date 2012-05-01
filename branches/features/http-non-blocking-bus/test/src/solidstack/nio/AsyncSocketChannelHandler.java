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
public class AsyncSocketChannelHandler extends SocketChannelHandler implements Runnable
{
	private ReadListener listener;
	private AtomicBoolean running = new AtomicBoolean();

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public AsyncSocketChannelHandler( Dispatcher dispatcher, ReadListener listener )
	{
		super( dispatcher );

		this.listener = listener;
	}

	public void setListener( ReadListener listener )
	{
		this.listener = listener;
	}

	protected ReadListener getListener()
	{
		return this.listener;
	}

	protected boolean isRunningAndSet()
	{
		return !this.running.compareAndSet( false, true );
	}

	protected void endOfRunning()
	{
		this.running.set( false );
	}

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
					getListener().incoming( this );

					if( channel.isOpen() )
					{
						if( getInputStream().available() == 0 )
						{
							getDispatcher().listenRead( key );
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
					if( Dispatcher.debug )
						System.out.println( "Channel (" + DebugId.getId( channel ) + ") thread aborted" );
				}
				else
					if( Dispatcher.debug )
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
