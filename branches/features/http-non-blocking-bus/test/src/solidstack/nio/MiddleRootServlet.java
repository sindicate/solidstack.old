package solidstack.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import solidstack.httpclient.Request;
import solidstack.httpclient.Response;
import solidstack.httpclient.ResponseProcessor;
import solidstack.httpclient.nio.Client;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.lang.SystemException;


public class MiddleRootServlet implements Servlet
{
	private Client client;

	public MiddleRootServlet() throws IOException
	{
		this.client = new Client( "localhost", 8001, MiddleServer.dispatcher );
	}

	public void call( final RequestContext context )
	{
		Request request = new Request( "/" );
		request.setHeader( "Host", "www.nu.nl" );

		ResponseProcessor processor = new ResponseProcessor()
		{
			public void process( Response response )
			{
				if( Dispatcher.debug )
					System.out.println( response.getHttpVersion() + " " + response.getStatus() + " " + response.getReason() );

				RequestContext c = context;
				solidstack.httpserver.Response r = c.getResponse();

				Map<String, String> headers = response.getHeaders();
				for( Entry<String, String> entry : headers.entrySet() )
				{
					if( Dispatcher.debug )
						System.out.println( entry.getKey() + ": " + entry.getValue() );
					r.setHeader( entry.getKey(), entry.getValue() );
				}
				if( Dispatcher.debug )
					System.out.println();

				OutputStream out = r.getOutputStream();
				InputStream in = response.getInputStream();
				byte[] buffer = new byte[ 8192 ];
				try
				{
					int len = in.read( buffer );
					while( len >= 0 )
					{
						out.write( buffer, 0, len );
						len = in.read( buffer );
					}
				}
				catch( IOException e )
				{
					throw new SystemException( e );
				}

				r.finish();

//				// TODO Detect Connection: close headers on the request & response
//				// TODO A GET request has no body, when a POST comes without content size, the connection should be closed.
//				// TODO What about socket.getKeepAlive() and the other properties?
//
//				String length = response.getHeader( "Content-Length" );
//				if( length == null )
//				{
//					String transfer = response.getHeader( "Transfer-Encoding" );
//					if( !"chunked".equals( transfer ) )
//						channel.close();
//				}
//
//				if( channel.isOpen() )
//					if( request.isConnectionClose() )
//						channel.close();
			}
		};

		try
		{
			this.client.request( request, processor );
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}

		context.setAsync( true );
	}
}
