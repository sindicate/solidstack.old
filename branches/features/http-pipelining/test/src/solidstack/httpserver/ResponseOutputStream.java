package solidstack.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.io.FatalIOException;
import solidstack.lang.Assert;


public class ResponseOutputStream extends OutputStream
{
	protected OutputStream out;
//	protected Response response;
	protected byte[] buffer = new byte[ 8192 ];
	protected int pos;
	protected boolean committed;
	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
	private boolean connectionClose;
	protected String contentType;
	protected String charSet;
	protected int statusCode = 200;
	protected String statusMessage = "OK";

	public ResponseOutputStream()
	{

	}

	public ResponseOutputStream( OutputStream out, boolean connectionClose )
	{
		this.out = out;
		this.connectionClose = connectionClose;
	}

	@Override
	public void write( byte[] b, int off, int len )
	{
		try
		{
			if( this.committed )
			{
//				System.out.write( b, off, len );
				this.out.write( b, off, len );
			}
			else if( this.buffer.length - this.pos < len )
			{
				writeHeader();
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b, off, len );
				this.out.write( b, off, len );
				this.committed = true;
			}
			else
			{
				System.arraycopy( b, off, this.buffer, this.pos, len );
				this.pos += len;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( byte[] b )
	{
		try
		{
			if( this.committed )
			{
//				System.out.write( b );
				this.out.write( b );
			}
			else if( this.buffer.length - this.pos < b.length )
			{
				writeHeader();
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b );
				this.out.write( b );
				this.committed = true;
			}
			else
			{
				System.arraycopy( b, 0, this.buffer, this.pos, b.length );
				this.pos += b.length;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( int b )
	{
		try
		{
			if( this.committed )
			{
//				System.out.write( b );
				this.out.write( b );
			}
			else if( this.buffer.length - this.pos < 1 )
			{
				writeHeader();
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b );
				this.out.write( b );
				this.committed = true;
			}
			else
			{
				this.buffer[ this.pos ] = (byte)b;
				this.pos ++;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void close()
	{
		flush();
		if( this.out instanceof ChunkedOutputStream )
			try
			{
				this.out.close();
			}
			catch( IOException e )
			{
				throw new HttpException( e );
			}
	}

	private void commit()
	{
		try
		{
			if( !this.committed )
			{
				writeHeader();
				// The outputstream may be changed at this point
				this.out.write( this.buffer, 0, this.pos );
				this.committed = true;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	public boolean isCommitted()
	{
		return this.committed;
	}

	@Override
	public void flush()
	{
		commit();
		try
		{
			this.out.flush();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	public void clear()
	{
		Assert.isFalse( this.committed );
		this.pos = 0;
	}

	public void setStatusCode( int code, String message )
	{
		if( this.committed )
			throw new IllegalStateException( "Response is already committed" );
		this.statusCode = code;
		this.statusMessage = message;
	}

	public void setContentType( String contentType, String charSet )
	{
		if( this.committed )
			throw new IllegalStateException( "Response is already committed" );
		this.contentType = contentType;
		this.charSet = charSet;
	}

	public void setHeader( String name, String value )
	{
		if( this.committed )
			throw new IllegalStateException( "Response is already committed" );
//		if( name.equals( "Content-Type" ) )
//			throw new IllegalArgumentException( "Content type should be set with setContentType()" );
		// TODO Interpret content-type, or not?
		setHeader0( name, value );
	}

	protected void setHeader0( String name, String value )
	{
		List< String > values = new ArrayList< String >();
		values.add( value );
		this.headers.put( name, values );
	}

	public String getHeader( String name )
	{
		List< String > values = this.headers.get( name );
		if( values == null )
			return null;
		Assert.isTrue( !values.isEmpty() );
		if( values.size() > 1 )
			throw new IllegalStateException( "Found more than 1 value for the header " + name );
		return values.get( 0 );
	}

	public void setCookie( String name, String value )
	{
		// TODO The path should be configurable
		setHeader( "Set-Cookie", name + "=" + value + "; Path=/" );
	}

	static public final byte[] HTTP = "HTTP/1.1 ".getBytes();
	static public final byte[] NEWLINE = new byte[] { '\r', '\n' };
	static public final byte[] COLON = new byte[] { ':', ' ' };

	private void writeHeader()
	{
		OutputStream out = this.out;

		if( this.connectionClose )
			setHeader( "Connection", "close" );
		else
			if( getHeader( "Content-Length" ) == null ) // TODO What about empty string?
				setHeader0( "Transfer-Encoding", "chunked" );

		// TODO status 404 and chunked encoding conflict each other

		if( this.contentType != null )
			if( this.charSet != null )
				setHeader0( "Content-Type", this.contentType + "; charset=" + this.charSet );
			else
				setHeader0( "Content-Type", this.contentType );

		try
		{
			out.write( HTTP );
			out.write( Integer.toString( this.statusCode ).getBytes() );
			out.write( ' ' );
			out.write( this.statusMessage.getBytes() );
			out.write( NEWLINE );
			for( Map.Entry< String, List< String > > entry : this.headers.entrySet() )
				for( String value : entry.getValue() )
				{
					out.write( entry.getKey().getBytes() );
					out.write( COLON );
					out.write( value.getBytes() );
					out.write( NEWLINE );
				}
			out.write( NEWLINE );
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}

		// TODO Are these header names case sensitive or not? And the values like 'chunked'?
		if( "chunked".equals( getHeader( "Transfer-Encoding" ) ) )
			this.out = new ChunkedOutputStream( this.out );
	}
}
