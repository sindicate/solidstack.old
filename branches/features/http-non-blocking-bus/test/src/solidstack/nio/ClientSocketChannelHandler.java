package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import solidstack.httpserver.ApplicationContext;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class ClientSocketChannelHandler extends SocketChannelHandler
{
	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public ClientSocketChannelHandler( Dispatcher dispatcher, SelectionKey key )
	{
		super( dispatcher, key );
	}

	@Override
	public void incoming() throws IOException
	{
		// TODO Need to do this another way
	}

	@Override
	public void run()
	{
		// TODO Need to do this another way
	}
}
