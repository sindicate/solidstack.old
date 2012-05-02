package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import solidstack.httpserver.ApplicationContext;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class SocketChannelHandler
{
	private int id;
	private Dispatcher dispatcher;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;
	public AtomicBoolean busy = new AtomicBoolean();

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public SocketChannelHandler( Dispatcher dispatcher )
	{
		this.dispatcher = dispatcher;

		this.in = new SocketChannelInputStream( this );
		this.out = new SocketChannelOutputStream( this );

		this.id = DebugId.getId( this );
	}

	public int getId()
	{
		return this.id;
	}

	public void setKey( SelectionKey key )
	{
		this.key = key;
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
