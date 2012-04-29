package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.ApplicationContext;


/**
 * Thread that handles an incoming connection.
 *
 * @author Ren� M. de Bloois
 */
abstract public class SocketChannelHandler
{
	private Dispatcher dispatcher;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;

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

	public void close() throws IOException
	{
		getChannel().close();
	}
}
