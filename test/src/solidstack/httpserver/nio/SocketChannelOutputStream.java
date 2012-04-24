package solidstack.httpserver.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.FatalSocketException;
import solidstack.lang.Assert;


// TODO Improve performance?
public class SocketChannelOutputStream extends OutputStream
{
	protected SocketChannel channel;
	protected SelectionKey key;
	protected ByteBuffer buffer;

	public SocketChannelOutputStream( SocketChannel channel, SelectionKey key )
	{
		this.channel = channel;
		this.key = key;
		this.buffer = ByteBuffer.allocate( 8192 );
	}

	@Override
	public void write( int b )
	{
		Assert.isTrue( this.buffer.hasRemaining() );
		this.buffer.put( (byte)b );
		if( !this.buffer.hasRemaining() )
			writeChannel();
	}

	@Override
	public void write( byte[] b, int off, int len )
	{
		while( len > 0 )
		{
			int l = len;
			if( l > this.buffer.remaining() )
				l = this.buffer.remaining();
			this.buffer.put( b, off, l );
			off += l;
			len -= l;
			if( !this.buffer.hasRemaining() )
				writeChannel();
		}
	}

	@Override
	public void flush() throws IOException
	{
		if( this.buffer.position() > 0 )
			writeChannel();
	}

	@Override
	public void close() throws IOException
	{
		flush();
	}

	protected void logBuffer( ByteBuffer buffer )
	{
		byte[] bytes = buffer.array();
		int end = buffer.limit();
		for( int i = 0; i < end; i++ )
		{
			String s = Integer.toHexString( bytes[ i ] );
			s = "00" + s;
			s = s.substring( s.length() - 2 );
			System.out.print( s + " " );
		}
		System.out.println();
	}

	protected void writeChannel()
	{
		Assert.isTrue( this.channel.isOpen() && this.channel.isConnected() );
		this.buffer.flip();
		Assert.isTrue( this.buffer.hasRemaining() );

		try
		{
			logBuffer( this.buffer );
			int written = this.channel.write( this.buffer );
			System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") written #" + written + " bytes to channel (1)" );
			while( this.buffer.hasRemaining() )
			{
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Waiting for write" );
				synchronized( this.key )
				{
					this.key.interestOps( this.key.interestOps() | SelectionKey.OP_WRITE );
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
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Waiting for write, ready" );
				logBuffer( this.buffer );
				written = this.channel.write( this.buffer );
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") written #" + written + " bytes to channel (2)" );
			}

			this.buffer.clear();
		}
		catch( IOException e )
		{
			throw new FatalSocketException( e );
		}
	}
}
