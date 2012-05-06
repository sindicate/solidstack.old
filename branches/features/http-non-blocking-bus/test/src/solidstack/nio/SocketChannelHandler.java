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
public class SocketChannelHandler
{
	private int id;
	private Dispatcher dispatcher;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;

	private HandlerPool pool;
	private long addedToPool;

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

		this.id = -1;
	}

	public int getId()
	{
		return this.id;
	}

	void setKey( SelectionKey key )
	{
		this.key = key;
		this.id = DebugId.getId( key.channel() );
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

	SocketChannel getChannel()
	{
		return (SocketChannel)this.key.channel();
	}

	SelectionKey getKey()
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

	public boolean isOpen()
	{
		return this.key.channel().isOpen();
	}

	public void close()
	{
		close0();
		if( this.pool != null )
			this.pool.channelClosed( this );
	}

	public void lost()
	{
		close0();
		if( this.pool != null )
			this.pool.channelLost( this );
	}

	public void close0()
	{
		this.key.cancel();
		if( isOpen() )
		{
			Loggers.nio.trace( "Channel ({}) Closed", getId() );
			try
			{
				this.key.channel().close();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
		}
	}

	public void setPool( HandlerPool pool )
	{
		this.pool = pool;
	}

	public long addedToPool()
	{
		return this.addedToPool;
	}

	public void addedToPool( long millis )
	{
		this.addedToPool = millis;
	}

	public void poolTimeout()
	{
		close0();
	}
}
