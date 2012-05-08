package solidstack.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.io.FatalIOException;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class SocketChannelHandler
{
	private Dispatcher dispatcher;
	private SelectionKey key;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;

	private HandlerPool pool;
	private long lastPooled;

	private int debugId;


	public SocketChannelHandler( Dispatcher dispatcher )
	{
		this.dispatcher = dispatcher;

		this.in = new SocketChannelInputStream( this );
		this.out = new SocketChannelOutputStream( this );

		this.debugId = -1;
	}

	void setKey( SelectionKey key )
	{
		this.key = key;
		this.debugId = DebugId.getId( key.channel() );
	}

	public void setPool( HandlerPool pool )
	{
		this.pool = pool;
	}

	int getDebugId()
	{
		return this.debugId;
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

	void dataIsReady()
	{
		synchronized( this.in )
		{
			this.in.notify();
		}
	}

	void writeIsReady()
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

	void lost()
	{
		close0();
		if( this.pool != null )
			this.pool.channelLost( this );
	}

	void poolTimeout()
	{
		close0();
	}

	// TODO Make this package private
	public void timeout()
	{
		Loggers.nio.trace( "Channel ({}) Timeout", getDebugId() );
		close();
	}

	private void close0()
	{
		this.key.cancel();
		if( isOpen() )
		{
			Loggers.nio.trace( "Channel ({}) Closed", getDebugId() );
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

	long lastPooled()
	{
		return this.lastPooled;
	}

	void returnToPool()
	{
		this.pool.putHandler( this );
		this.lastPooled = System.currentTimeMillis();
	}
}
