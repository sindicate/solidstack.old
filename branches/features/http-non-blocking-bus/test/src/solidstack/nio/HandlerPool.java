package solidstack.nio;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HandlerPool
{
	private List<SocketChannelHandler> pool = new LinkedList<SocketChannelHandler>();
	private int total;

	public SocketChannelHandler getHandler()
	{
		synchronized( this.pool )
		{
			if( this.pool.isEmpty() )
				return null;
			return this.pool.remove( this.pool.size() - 1 );
		}
	}

	public void putHandler( SocketChannelHandler handler )
	{
		handler.addedToPool( System.currentTimeMillis() );
		synchronized( this.pool )
		{
			this.pool.add( handler );
		}
	}

	public void add()
	{
		this.total++;
	}

	public void remove()
	{
		this.total--;
	}

	public void channelClosed( SocketChannelHandler handler )
	{
		synchronized( this.pool )
		{
			this.pool.remove( handler );
			remove();
		}
	}

	public int size()
	{
		return this.pool.size();
	}

	public int total()
	{
		return this.total;
	}

	public void timeout()
	{
		long now = System.currentTimeMillis();
		synchronized( this.pool )
		{
			for( Iterator<SocketChannelHandler> i = this.pool.iterator(); i.hasNext(); )
			{
				SocketChannelHandler handler = i.next();
				if( handler.addedToPool() + 10000 <= now )
				{
					handler.poolTimeout();
					i.remove();
				}
			}
		}
	}
}
