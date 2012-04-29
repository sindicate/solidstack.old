package solidstack.httpclient.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import solidstack.httpclient.ChunkedInputStream;
import solidstack.httpclient.Request;
import solidstack.httpclient.RequestWriter;
import solidstack.httpclient.Response;
import solidstack.httpclient.ResponseProcessor;
import solidstack.httpserver.HttpBodyInputStream;
import solidstack.httpserver.HttpException;
import solidstack.httpserver.HttpHeaderTokenizer;
import solidstack.httpserver.Token;
import solidstack.io.FatalIOException;
import solidstack.nio.Dispatcher;
import solidstack.nio.SocketChannelHandler;


public class Client extends Thread
{
	private Dispatcher dispatcher;
	private String hostname;

	public Client( String hostname, Dispatcher dispatcher ) throws IOException
	{
		this.dispatcher = dispatcher;
		this.hostname = hostname;
	}

	private void sendRequest( Request request, OutputStream out ) throws IOException
	{
		RequestWriter writer = new RequestWriter( out, "ISO-8859-1" );
		writer.write( "GET " );
		String path = request.getPath();
		writer.write( path.length() > 0 ? path : "/" );
		writer.write( " HTTP/1.1\r\n" );
		for( Map.Entry< String, List< String > > entry : request.getHeaders().entrySet() )
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
	}

	private Response receiveResponse( InputStream in )
	{
		Response result = new Response();

		HttpHeaderTokenizer tokenizer = new HttpHeaderTokenizer( in );

		String line = tokenizer.getLine();
		String[] parts = line.split( "[ \t]+" );

		if( !parts[ 0 ].equals( "HTTP/1.1" ) )
			throw new HttpException( "Only HTTP/1.1 responses are supported" );

		result.setHttpVersion( parts[ 0 ] );
		result.setStatus( Integer.parseInt( parts[ 1 ] ) );
		result.setReason( parts[ 2 ] );

		Token field = tokenizer.getField();
		while( !field.isEndOfInput() )
		{
			Token value = tokenizer.getValue();
//			System.out.println( "    "+ field.getValue() + " = " + value.getValue() );
			result.addHeader( field.getValue(), value.getValue() );
			field = tokenizer.getField();
		}

		String length = result.getHeader( "Content-Length" );
		if( length != null )
		{
			int l = Integer.parseInt( length );
			result.setInputStream( new HttpBodyInputStream( in, l ) );
		}
		else
		{
			String encoding = result.getHeader( "Transfer-Encoding" );
			if( "chunked".equals( encoding ) )
//				result.setInputStream( in );
				result.setInputStream( new ChunkedInputStream( in ) );
		}

		// TODO Which error codes do not contain a body?

		return result;

		// TODO Detect Connection: close headers on the request & response
		// TODO What about socket.getKeepAlive() and the other properties?

//		String length = getHeader( "Content-Length" );
//		Assert.notNull( length );
//		int l = Integer.parseInt( length );
//		this.bodyIn = new HttpBodyInputStream( in, l );

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

	public void request( Request request, ResponseProcessor responseProcessor ) throws IOException
	{
		SocketChannelHandler handler = this.dispatcher.connect( this.hostname, 80 );
		try
		{
			sendRequest( request, handler.getOutputStream() );
			Response response = receiveResponse( handler.getInputStream() );
			InputStream in = response.getInputStream();
			responseProcessor.process( response );
			drain( in, System.out );
		}
		finally
		{
			handler.close();
		}
	}

	private void drain( InputStream in, PrintStream out )
	{
		if( in == null )
			return;
		try
		{
			int i = in.read();
			while( i >= 0 )
			{
				if( out != null )
					out.print( (char)i );
				i = in.read();
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
