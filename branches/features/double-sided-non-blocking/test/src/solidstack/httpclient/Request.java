package solidstack.httpclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.httpserver.ChunkedOutputStream;
import solidstack.httpserver.FlushBlockingOutputStream;
import solidstack.lang.Assert;


public class Request
{
//	static protected int count = 1;

	// TODO GET or POST
	protected String path;
	protected RequestOutputStream out;
	protected InputStream in;
	protected RequestWriter writer;
	protected PrintWriter printWriter;
	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
	protected boolean committed;
	protected String contentType;
	protected String charSet;

	public Request( OutputStream out, InputStream in )
	{
		this.out = new RequestOutputStream( this, out );
		this.in = in;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public RequestOutputStream getOutputStream()
	{
		return this.out;
	}

	public RequestWriter getWriter()
	{
		if( this.writer != null )
			return this.writer;
		if( this.charSet != null )
			return getWriter( this.charSet );
		return getWriter( "ISO-8859-1" );
	}

	public RequestWriter getWriter( String encoding )
	{
		if( this.writer != null )
		{
			if( this.writer.getEncoding().equals( encoding ) )
				return this.writer;
			this.writer.flush();
		}
		return this.writer = new RequestWriter( this.out, encoding );
	}

	public PrintWriter getPrintWriter( String encoding )
	{
		return new PrintWriter( getWriter( encoding ) );
	}

	public void setHeader( String name, String value )
	{
		if( this.committed )
			throw new IllegalStateException( "Request is already committed" );
		if( name.equals( "Content-Type" ) )
			throw new IllegalArgumentException( "Content type should be set with setContentType()" );
		setHeader0( name, value );
	}

	protected void setHeader0( String name, String value )
	{
		List< String > values = new ArrayList< String >();
		values.add( value );
		this.headers.put( name, values );
	}

//Host: www.nu.nl
//Connection: keep-alive
//Cache-Control: max-age=0
//User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Encoding: gzip,deflate,sdch
//Accept-Language: en-US,nl-NL;q=0.8,en;q=0.6,nl;q=0.4
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
//If-Modified-Since: Fri, 27 Apr 2012 17:52:18 GMT

	public void writeHeader( OutputStream out )
	{
//		if( getHeader( "Content-Length" ) == null ) // TODO What about empty string?
//			setHeader0( "Transfer-Encoding", "chunked" );

		// TODO status 404 and chunked encoding conflict each other

//		if( this.contentType != null )
//			if( this.charSet != null )
//				setHeader0( "Content-Type", this.contentType + "; charset=" + this.charSet );
//			else
//				setHeader0( "Content-Type", this.contentType );

//		System.out.println( "Response:" );
		RequestWriter writer = new RequestWriter( new FlushBlockingOutputStream( out ), "ISO-8859-1" );
		writer.write( "GET " );
		writer.write( this.path ); // TODO Must be at least /
		writer.write( " HTTP/1.1\r\n" );
		for( Map.Entry< String, List< String > > entry : this.headers.entrySet() )
			for( String value : entry.getValue() )
			{
				writer.write( entry.getKey() );
				writer.write( ": " );
				writer.write( value );
				writer.write( "\r\n" );
//				System.out.println( "    " + entry.getKey() + " = " + value );
			}
		writer.write( "\r\n" );
		writer.flush();
		this.committed = true;

		// TODO Are these header names case sensitive or not? And the values like 'chunked'?
		if( "chunked".equals( getHeader( "Transfer-Encoding" ) ) )
			this.out.out = new ChunkedOutputStream( this.out.out );
	}

	public boolean isCommitted()
	{
		return this.committed;
	}

	public void reset()
	{
		if( this.committed )
			throw new IllegalStateException( "Response is already committed" );
		getOutputStream().clear();
		this.writer = null;
		this.headers.clear();
	}

	public void flush()
	{
		if( this.writer != null )
			this.writer.flush();
		getOutputStream().flush();
	}

	public void finish()
	{
		flush();
//		getOutputStream().close();
	}

	public void setContentType( String contentType, String charSet )
	{
		if( this.committed )
			throw new IllegalStateException( "Response is already committed" );
		this.contentType = contentType;
		this.charSet = charSet;
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

	public Response getResponse() throws UnsupportedEncodingException
	{
		return new Response( this.in );
	}
}
