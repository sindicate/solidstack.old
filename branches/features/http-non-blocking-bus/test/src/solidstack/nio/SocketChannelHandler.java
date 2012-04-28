package solidstack.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.ApplicationContext;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
abstract public class SocketChannelHandler implements Runnable
{
	private SocketChannel channel;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public SocketChannelHandler( SocketChannel channel, SelectionKey key )
	{
		this.channel = channel;
		this.key = key;

		this.in = new SocketChannelInputStream( channel, key );
		this.out = new SocketChannelOutputStream( channel, key );
	}

	public SocketChannelInputStream getInputStream()
	{
		return this.in;
	}

	public SocketChannelOutputStream getOutputStream()
	{
		return this.out;
	}

	public SocketChannel getChannel()
	{
		return this.channel;
	}

	public SelectionKey getKey()
	{
		return this.key;
	}

	abstract public void run();

	public void dataReady()
	{
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Data ready, notify" );
		synchronized( this.in )
		{
			this.in.notify();
		}
	}

	public void writeReady()
	{
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Write ready, notify" );
		synchronized( this.out )
		{
			this.out.notify();
		}
	}
}
