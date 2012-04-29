package solidstack.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
abstract public class ServerSocketChannelHandler
{
	private ServerSocketChannel server;
	private SelectionKey key;

	public void setServer( ServerSocketChannel server )
	{
		this.server = server;
	}

	public ServerSocketChannel getServer()
	{
		return this.server;
	}

	public void setKey( SelectionKey key )
	{
		this.key = key;
	}

	abstract public SocketChannelHandler incoming( Dispatcher dispatcher, SelectionKey key );
}
