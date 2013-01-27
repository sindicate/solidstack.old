/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.io.FatalIOException;
import solidstack.lang.Assert;


public class Response
{
//	static protected int count = 1;

	protected Request request;
	protected ResponseOutputStream out;
	protected ResponseWriter writer;
	protected PrintWriter printWriter;
	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
	protected int statusCode = 200;
	protected String statusMessage = "OK";
	protected String contentType;
	protected String charSet;

	protected Response()
	{
	}

	public Response( Request request, OutputStream out )
	{
		this.request = request;
		this.out = new ResponseOutputStream( this, out );
	}

	public ResponseOutputStream getOutputStream()
	{
		return this.out;
	}

	public ResponseWriter getWriter()
	{
		if( this.writer != null )
			return this.writer;
		if( this.charSet != null )
			return getWriter( this.charSet );
		return getWriter( "ISO-8859-1" );
	}

	public ResponseWriter getWriter( String encoding )
	{
		if( this.writer != null )
		{
			if( this.writer.getEncoding().equals( encoding ) )
				return this.writer;
			this.writer.flush();
		}
		return this.writer = new ResponseWriter( this.out, encoding );
	}

	public PrintWriter getPrintWriter( String encoding )
	{
		return new PrintWriter( getWriter( encoding ) );
	}

	public void setHeader( String name, String value )
	{
		if( this.out.committed )
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

	static public final byte[] HTTP = "HTTP/1.1 ".getBytes();
	static public final byte[] NEWLINE = new byte[] { '\r', '\n' };
	static public final byte[] COLON = new byte[] { ':', ' ' };

	public void writeHeader( OutputStream out )
	{
		if( this.request.isConnectionClose() )
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
			this.out.out = new ChunkedOutputStream( this.out.out );
	}

	public boolean isCommitted()
	{
		return this.out.committed;
	}

	public void setStatusCode( int code, String message )
	{
		if( this.out.committed )
			throw new IllegalStateException( "Response is already committed" );
		this.statusCode = code;
		this.statusMessage = message;
	}

	public void reset()
	{
		if( this.out.committed )
			throw new IllegalStateException( "Response is already committed" );
		getOutputStream().clear();
		this.writer = null;
		this.statusCode = 200;
		this.statusMessage = "OK";
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
		getOutputStream().close();
	}

	public void setContentType( String contentType, String charSet )
	{
		if( this.out.committed )
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

	public void setCookie( String name, String value )
	{
		setHeader( "Set-Cookie", name + "=" + value );
	}
}
