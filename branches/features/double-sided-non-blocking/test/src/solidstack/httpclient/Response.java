package solidstack.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.HttpHeaderTokenizer;
import solidstack.httpserver.RequestTokenizer;
import solidstack.httpserver.Token;
import solidstack.io.PushbackReader;
import solidstack.io.ReaderSourceReader;
import solidstack.lang.SystemException;

public class Response
{
	private InputStream in;

	public Response( InputStream in ) throws UnsupportedEncodingException
	{
		this.in = in;

		PushbackReader reader = new PushbackReader( new ReaderSourceReader( new BufferedReader( new InputStreamReader( in, "ISO-8859-1" ) ) ) );
		RequestTokenizer requestTokenizer = new RequestTokenizer( reader );

		Token token = requestTokenizer.get();
		if( !token.equals( "HTTP/1.1" ) )
			throw new HttpException( "Only HTTP/1.1 responses are supported" );

		token = requestTokenizer.get(); // TODO Result
		token = requestTokenizer.get(); // TODO OK
		requestTokenizer.getNewline();

		HttpHeaderTokenizer headerTokenizer = new HttpHeaderTokenizer( reader );
		Token field = headerTokenizer.getField();
		while( !field.isEndOfInput() )
		{
			Token value = headerTokenizer.getValue();
			System.out.println( "    "+ field.getValue() + " = " + value.getValue() );
			addHeader( field.getValue(), value.getValue() );
			field = headerTokenizer.getField();
		}
		requestTokenizer.getNewline();

		// TODO Detect Connection: close headers on the request & response
		// TODO What about socket.getKeepAlive() and the other properties?

//		String length = getHeader( "Content-Length" );
//		if( length == null )
//		{
//			String transfer = response.getHeader( "Transfer-Encoding" );
//			if( !"chunked".equals( transfer ) )
//				this.socket.close();
//		}
//
//		if( !this.socket.isClosed() )
//			if( request.isConnectionClose() )
//				this.socket.close();
//		if( this.socket.isClosed() )
//			return;
//		if( !this.socket.isThreadPerConnection() )
//			if( in.available() <= 0 )
//				return;
	}

	private void addHeader( String value, String value2 )
	{
		// TODO Auto-generated method stub

	}

	public void print()
	{
		byte[] buffer = new byte[ 4096 ];
		try
		{
			int len = this.in.read( buffer );
			while( len >= 0 )
			{
				System.out.write( buffer, 0, len );
				len = this.in.read( buffer );
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}
}
