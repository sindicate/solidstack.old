package solidstack.httpserver.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.FatalSocketException;
import solidstack.lang.Assert;


public class SocketChannelInputStream extends InputStream
{
	protected SocketChannel channel;
	protected SelectionKey key;
	protected ByteBuffer buffer;

	public SocketChannelInputStream( SocketChannel channel, SelectionKey key )
	{
		this.channel = channel;
		this.key = key;
		this.buffer = ByteBuffer.allocate( 1024 );
		this.buffer.flip();
	}

	@Override
	public int read() throws IOException
	{
		if( !this.buffer.hasRemaining() )
		{
			if( this.channel == null )
				return -1;
			readChannel();
		}

		return this.buffer.get();
	}

	@Override
	public int read( byte[] b, int off, int len ) throws IOException
	{
		if( !this.buffer.hasRemaining() )
		{
			if( this.channel == null )
				return -1;
			readChannel();
		}

		if( len > this.buffer.remaining() )
			len = this.buffer.remaining();
		this.buffer.get( b, off, len );
		return len;
	}

	@Override
	public int available() throws IOException
	{
		return this.buffer.remaining();
	}

	// TODO What if it read too much? Like when 2 requests are chained. The handler needs to keep reading.
	protected void readChannel()
	{
		Assert.isFalse( this.buffer.hasRemaining() );
		Assert.isTrue( this.channel.isOpen() );

		this.buffer.clear();

		try
		{
			int read = this.channel.read( this.buffer );
			System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") read #" + read + " bytes from channel" );
			while( read == 0 )
			{
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Waiting for data" );
				synchronized( this.key )
				{
					this.key.interestOps( this.key.interestOps() | SelectionKey.OP_READ );
				}
				this.key.selector().wakeup();
				try
				{
					synchronized( this )
					{
						wait();
					}
				}
				catch( InterruptedException e )
				{
					throw new FatalSocketException( e );
				}
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Waiting for data, ready" );

				read = this.channel.read( this.buffer );
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") read #" + read + " bytes from channel" );
			}

			if( read == -1 )
			{
				this.key.cancel();
				this.channel.close();
				this.channel = null;
			}
			else
				this.buffer.flip();
		}
		catch( IOException e )
		{
			throw new FatalSocketException( e );
		}
	}
}
