package solidstack.httpserver.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.FatalSocketException;
import solidstack.httpserver.HttpException;
import solidstack.httpserver.HttpHeaderTokenizer;
import solidstack.httpserver.Request;
import solidstack.httpserver.RequestContext;
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
	private SocketChannel channel;
	private SelectionKey key;
	private ApplicationContext applicationContext;
	private SocketChannelInputStream in;
	private SocketChannelOutputStream out;
	private Executor executor;
	volatile private boolean running;

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public Handler( SocketChannel channel, SelectionKey key, ApplicationContext applicationContext, Executor executor )
	{
		this.channel = channel;
		this.key = key;
		this.applicationContext = applicationContext;
		this.executor = executor;

		this.in = new SocketChannelInputStream( channel, key );
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
		boolean finished = false;
		try
		{
			try
			{
				while( true )
				{
					// TODO Use a PushbackInputStream
					PushbackReader reader = new PushbackReader( new ReaderSourceReader( new BufferedReader( new InputStreamReader( this.in, "ISO-8859-1" ) ) ) );

					Request request = new Request();

					HttpHeaderTokenizer tokenizer = new HttpHeaderTokenizer( this.in );

					String line = tokenizer.getLine();
					String[] parts = line.split( "[ \t]+" );

					request.setMethod( parts[ 0 ] );

					String url = parts[ 1 ];
					if( !parts[ 2 ].equals( "HTTP/1.1" ) )
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

					Token field = tokenizer.getField();
					while( !field.isEndOfInput() )
					{
						Token value = tokenizer.getValue();
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
						field = tokenizer.getField();
					}

					String contentType = request.getHeader( "Content-Type" );
					if( "application/x-www-form-urlencoded".equals( contentType ) )
					{
						String contentLength = request.getHeader( "Content-Length" );
						if( contentLength != null )
						{
							int len = Integer.parseInt( contentLength );
							UrlEncodedParser parser = new UrlEncodedParser( this.in, len );
							String parameter = parser.getParameter();
							while( parameter != null )
							{
								String value = parser.getValue();
								request.addParameter( parameter, value );
								parameter = parser.getParameter();
							}
						}
					}

					Response response = new Response( request, this.out ); // out is a SocketChannelOutputStream, close() does not close the SocketChannel
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

					if( this.channel.isOpen() )
					{
						if( this.in.available() == 0 )
						{
							System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Waiting for data" );
							synchronized( this.key )
							{
								this.key.interestOps( this.key.interestOps() | SelectionKey.OP_READ );
								this.key.selector().wakeup();
							}
							finished = true;
							return;
						}
					}
					else
					{
						finished = true;
						return;
					}
				}
			}
			finally
			{
				synchronized( this )
				{
					this.running = false;
				}
				if( !finished )
				{
					this.channel.close();
				}
				// TODO Synchronization
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") thread ended" );
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public void dataReady()
	{
		// TODO Synchronization
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Data ready, notify" );
		synchronized( this )
		{
			if( !this.running )
			{
				this.executor.execute( this );
				this.running = true;
				System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") created/starting thread" );
			}
			synchronized( this.in )
			{
				this.in.notify();
			}
		}
	}

	public void writeReady()
	{
		System.out.println( "Channel (" + DebugId.getId( this.channel ) + ") Write ready, notify" );
		Assert.isTrue( this.running );
		synchronized( this.out )
		{
			this.out.notify();
		}
	}
}
