package solidstack.httpserver;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;


// TODO It overrides OutputStream, shouldn't we throw IOExceptions?
public class GZipResponseOutputStream extends ResponseOutputStream
{
	protected ResponseOutputStream response;
	protected GZIPOutputStream out;


	public GZipResponseOutputStream( ResponseOutputStream out )
	{
		this.response = out;
		try
		{
			this.out = new GZIPOutputStream( out );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( byte[] b, int off, int len )
	{
		try
		{
			this.out.write( b, off, len );
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
			this.out.write( b );
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
			this.out.write( b );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void close()
	{
		try
		{
			this.out.close();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void flush()
	{
		try
		{
			this.out.flush();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void clear()
	{
		this.response.clear();
		try
		{
			this.out = new GZIPOutputStream( this.response );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public boolean isCommitted()
	{
		return this.response.isCommitted();
	}

	@Override
	public void setStatusCode( int code, String message )
	{
		this.response.setStatusCode( code, message );
	}

	@Override
	public void setContentType( String contentType, String charSet )
	{
		this.response.setContentType( contentType, charSet );
	}

	@Override
	public void setHeader( String name, String value )
	{
		this.response.setHeader( name, value );
	}

	@Override
	protected void setHeader0( String name, String value )
	{
		this.response.setHeader0( name, value );
	}

	@Override
	public String getHeader( String name )
	{
		return this.response.getHeader( name );
	}

	@Override
	public void setCookie( String name, String value )
	{
		this.response.setCookie( name, value );
	}
}
