package solidstack.httpserver.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.CloseBlockingOutputStream;
import solidstack.httpserver.FatalSocketException;
import solidstack.httpserver.HttpException;
import solidstack.httpserver.HttpHeaderTokenizer;
import solidstack.httpserver.Request;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.RequestTokenizer;
import solidstack.httpserver.Response;
import solidstack.httpserver.Token;
import solidstack.httpserver.UrlEncodedParser;
import solidstack.io.PushbackReader;
import solidstack.io.ReaderSourceReader;
import solidstack.lang.Assert;
import solidstack.lang.SystemException;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class Handler implements Runnable
{
//	private Server server;
	private SocketChannel channel;
	private SelectionKey key;
	private ApplicationContext applicationContext;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;
	private Thread thread;
//	static int counter;

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public Handler( SocketChannel channel, SelectionKey key, ApplicationContext applicationContext )
	{
//		this.server = server;
		this.channel = channel;
		this.applicationContext = applicationContext;
		this.in = new SocketChannelInputStream( channel, key, this );
		this.out = new SocketChannelOutputStream( channel, key );
	}

	/**
	 * This method actually handles the connection.
	 *
	 * @throws IOException Whenever the socket throws an {@link IOException}.
	 */
	// TODO Check exception handling
	public void run()
	{
		try
		{
			try
			{
				// TODO Use a PushbackInputStream
				PushbackReader reader = new PushbackReader( new ReaderSourceReader( new BufferedReader( new InputStreamReader( this.in, "ISO-8859-1" ) ) ) );

				Request request = new Request();

				RequestTokenizer requestTokenizer = new RequestTokenizer( reader );
				Token token = requestTokenizer.get();
				request.setMethod( token.getValue() );

				String url = requestTokenizer.get().getValue();
				token = requestTokenizer.get();
				if( !token.equals( "HTTP/1.1" ) )
					throw new HttpException( "Only HTTP/1.1 requests are supported" );

				System.out.println( "GET " + url + " HTTP/1.1" );

				String parameters = null;
				int pos = url.indexOf( '?' );
				if( pos >= 0 )
				{
					parameters = url.substring( pos + 1 );
					url = url.substring( 0, pos );

					String[] pars = parameters.split( "&" );
					for( String par : pars )
					{
						pos = par.indexOf( '=' );
						if( pos >= 0 )
							request.addParameter( par.substring( 0, pos ), par.substring( pos + 1 ) );
						else
							request.addParameter( par, null );
					}
				}

				// TODO Fragment too? Maybe use the URI class?

				if( url.endsWith( "/" ) )
					url = url.substring( 0, url.length() - 1 );
				request.setUrl( url );
				request.setQuery( parameters );

				requestTokenizer.getNewline();

				HttpHeaderTokenizer headerTokenizer = new HttpHeaderTokenizer( reader );
				Token field = headerTokenizer.getField();
				while( !field.isEndOfInput() )
				{
					Token value = headerTokenizer.getValue();
					//			System.out.println( "    "+ field.getValue() + " = " + value.getValue() );
					if( field.equals( "Cookie" ) ) // TODO Case insensitive?
					{
						String s = value.getValue();
						int pos2 = s.indexOf( '=' );
						if( pos2 >= 0 )
							request.addCookie( s.substring( 0, pos2 ), s.substring( pos2 + 1 ) );
						else
							request.addHeader( field.getValue(), s );
					}
					else
					{
						request.addHeader( field.getValue(), value.getValue() );
					}
					field = headerTokenizer.getField();
				}

				String contentType = request.getHeader( "Content-Type" );
				if( "application/x-www-form-urlencoded".equals( contentType ) )
				{
					String contentLength = request.getHeader( "Content-Length" );
					if( contentLength != null )
					{
						int len = Integer.parseInt( contentLength );
						UrlEncodedParser parser = new UrlEncodedParser( reader, len );
						String parameter = parser.getParameter();
						while( parameter != null )
						{
							String value = parser.getValue();
							request.addParameter( parameter, value );
							parameter = parser.getParameter();
						}
					}
				}

				OutputStream out = new CloseBlockingOutputStream( this.out );
				Response response = new Response( request, this.out );
				RequestContext context = new RequestContext( request, response, this.applicationContext );
				try
				{
					this.applicationContext.dispatch( context );
				}
				catch( FatalSocketException e )
				{
					throw e;
				}
				catch( Exception e )
				{
					Throwable t = e;
					if( t.getClass().equals( HttpException.class ) && t.getCause() != null )
						t = t.getCause();
					t.printStackTrace( System.out );
					if( !response.isCommitted() )
					{
						response.reset();
						response.setStatusCode( 500, "Internal Server Error" );
						response.setContentType( "text/plain", "ISO-8859-1" );
						PrintWriter writer = response.getPrintWriter( "ISO-8859-1" );
						t.printStackTrace( writer );
						writer.flush();
					}
					// TODO Is the socket going to be closed?
				}

				response.finish();

				// TODO Detect Connection: close headers on the request & response
				// TODO A GET request has no body, when a POST comes without content size, the connection should be closed.
				// TODO What about socket.getKeepAlive() and the other properties?

				String length = response.getHeader( "Content-Length" );
				if( length == null )
				{
					String transfer = response.getHeader( "Transfer-Encoding" );
					if( !"chunked".equals( transfer ) )
						this.channel.close();
				}

				if( this.channel.isOpen() )
					if( request.isConnectionClose() )
						this.channel.close();
			}
			catch( RuntimeException e )
			{
				this.channel.close(); // TODO Is this good enough?
				e.printStackTrace( System.out );
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
		finally
		{
			System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") thread ended" );
		}
	}

	public void dataReady()
	{
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Data ready, notify" );
		if( this.thread == null )
		{
			this.thread = new Thread( this );
			System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") created/starting thread" );
			this.thread.start();
		}
		synchronized( this.in )
		{
			this.in.notify();
		}
	}

	public void writeReady()
	{
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Write ready, notify" );
		Assert.notNull( this.thread == null );
		synchronized( this.in )
		{
			this.out.notify();
		}
	}
}
