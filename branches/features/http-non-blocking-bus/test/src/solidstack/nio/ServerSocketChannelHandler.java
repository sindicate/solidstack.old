package solidstack.nio;

import java.nio.channels.SelectionKey;

import solidstack.httpserver.ApplicationContext;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class ServerSocketChannelHandler extends AsyncSocketChannelHandler
{
	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public ServerSocketChannelHandler( Dispatcher dispatcher, SelectionKey key )
	{
		super( dispatcher );

		setKey( key );
	}

	@Override
	public void run()
	{
		SelectionKey key = getKey();

		boolean complete = false;
		try
		{
			try
			{
				while( true )
				{
					getListener().incoming( this );

					if( isOpen() )
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
					close();
					if( Loggers.nio.isDebugEnabled() )
						Loggers.nio.trace( "Channel ({}) task aborted", getId() );
				}
				else
					if( Loggers.nio.isDebugEnabled() )
						Loggers.nio.trace( "Channel ({}) task complete", getId() );

				endOfRunning();
			}
		}
		catch( Throwable t ) // TODO Exception, not Throwable
		{
			Loggers.nio.debug( "Channel ({}) Unhandled exception", getId(), t );
		}
	}
}
