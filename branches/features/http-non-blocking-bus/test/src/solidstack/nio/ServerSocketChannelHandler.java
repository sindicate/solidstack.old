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
					// TODO Try catch/finally?
					getListener().incoming( this );

					if( isOpen() )
					{
						if( getInputStream().available() == 0 )
						{
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
						Loggers.nio.trace( "Channel ({}) task aborted", getDebugId() );
					endOfRunning();
				}
				else
				{
					if( Loggers.nio.isDebugEnabled() )
						Loggers.nio.trace( "Channel ({}) task complete", getDebugId() );
					endOfRunning();
					getDispatcher().listenRead( key ); // TODO The socket needs to be reading, otherwise client disconnects do not come through
				}
			}
		}
		catch( Throwable t ) // TODO Exception, not Throwable
		{
			Loggers.nio.debug( "Channel ({}) Unhandled exception", getDebugId(), t );
		}
	}
}
