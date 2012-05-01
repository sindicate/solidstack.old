package solidstack.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.FatalSocketException;
import solidstack.lang.Assert;


// TODO Improve performance?
public class SocketChannelOutputStream extends OutputStream
{
	private SocketChannelHandler handler;
	private ByteBuffer buffer;

	public SocketChannelOutputStream( SocketChannelHandler handler )
	{
		this.handler = handler;
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
		SocketChannel channel = this.handler.getChannel();
		int id = DebugId.getId( channel );

		Assert.isTrue( channel.isOpen() );
		Assert.isTrue( channel.isConnected() );
		this.buffer.flip();
		Assert.isTrue( this.buffer.hasRemaining() );

		try
		{
//			logBuffer( this.buffer );
			int written = channel.write( this.buffer );
			if( Dispatcher.debug )
				System.out.println( "Channel (" + id + ") written #" + written + " bytes to channel (1)" );
			while( this.buffer.hasRemaining() )
			{
				this.handler.getDispatcher().listenWrite( this.handler.getKey() );
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

//				logBuffer( this.buffer );
				written = channel.write( this.buffer );
				if( Dispatcher.debug )
					System.out.println( "Channel (" + id + ") written #" + written + " bytes to channel (2)" );
			}

			this.buffer.clear();
		}
		catch( IOException e )
		{
			throw new FatalSocketException( e );
		}
	}
}
