package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.ApplicationContext;
import solidstack.io.FatalIOException;


/**
 * Thread that handles an incoming connection.
 *
 * @author Ren� M. de Bloois
 */
abstract public class ServerSocketChannelHandler extends AsyncSocketChannelHandler
{
	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public ServerSocketChannelHandler( Dispatcher dispatcher, SelectionKey key )
	{
		super( dispatcher, key );
	}

	@Override
	abstract public void incoming() throws IOException;

	@Override
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

				endOfRunning();
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
