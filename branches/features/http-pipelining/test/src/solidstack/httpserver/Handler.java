package solidstack.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Thread that handles an incoming connection.
 *
 * @author René M. de Bloois
 */
public class Handler extends Thread
{
	private Socket socket;
	private ApplicationContext applicationContext;
//	static int counter;

	/**
	 * Constructor.
	 *
	 * @param socket The incoming connection.
	 * @param applicationContext The {@link ApplicationContext}.
	 */
	public Handler( Socket socket, ApplicationContext applicationContext )
	{
		this.socket = socket;
		this.applicationContext = applicationContext;
	}

	/**
	 * This method actually handles the connection.
	 *
	 * @throws IOException Whenever the socket throws an {@link IOException}.
	 */
	// TODO Check exception handling
	public void handle() throws IOException
	{
		try
		{
			try
			{
				while( true )
				{
					InputStream in = this.socket.getInputStream();

					Request request = new Request();

					HttpHeaderTokenizer tokenizer = new HttpHeaderTokenizer( in );

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
						field = tokenizer.getField();
					}

					String contentType = request.getHeader( "Content-Type" );
					if( "application/x-www-form-urlencoded".equals( contentType ) )
					{
						String contentLength = request.getHeader( "Content-Length" );
						if( contentLength != null )
						{
							int len = Integer.parseInt( contentLength );
							UrlEncodedParser parser = new UrlEncodedParser( in, len );
							String parameter = parser.getParameter();
							while( parameter != null )
							{
								String value = parser.getValue();
								request.addParameter( parameter, value );
								parameter = parser.getParameter();
							}
						}
					}

//					OutputStream out = this.socket.getOutputStream();
//					out = new CloseBlockingOutputStream( out );
//					Response response = new Response( request, out );
					RequestContext context = new RequestContext( request, this.applicationContext );
					ResponseOutputStream out = new ResponseOutputStream( this.socket.getOutputStream(), request.isConnectionClose() );
					try
					{
						// TODO 2 try catches, one for read one for write
						Response response = this.applicationContext.dispatch( context );
						response.write( out );
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
						if( !out.isCommitted() )
						{
							out.clear();
							out.setStatusCode( 500, "Internal Server Error" );
							out.setContentType( "text/plain", "ISO-8859-1" );
							PrintWriter writer = new PrintWriter( new OutputStreamWriter( out, "ISO-8859-1" ) );
							t.printStackTrace( writer );
							writer.flush();
						}
						// TODO Is the socket going to be closed?
					}

					out.close();

					// TODO Detect Connection: close headers on the request & response
					// TODO A GET request has no body, when a POST comes without content size, the connection should be closed.
					// TODO What about socket.getKeepAlive() and the other properties?

					String length = out.getHeader( "Content-Length" );
					if( length == null )
					{
						String transfer = out.getHeader( "Transfer-Encoding" );
						if( !"chunked".equals( transfer ) )
							this.socket.close();
					}

					if( !this.socket.isClosed() )
						if( request.isConnectionClose() )
							this.socket.close();

					//		}
					//		finally
					//		{
					//			file.close();
					//		}

					if( this.socket.isClosed() )
						return;
//					if( !this.socket.isThreadPerConnection() )
//						if( in.available() <= 0 )
//							return;
				}
			}
			catch( Exception e )
			{
				this.socket.close();
				e.printStackTrace( System.out );
			}
		}
		finally
		{
			end();
		}
	}

	public void end() throws IOException
	{

	}

	@Override
	public void run()
	{
		try
		{
			handle();
		}
		catch( Throwable t )
		{
			t.printStackTrace( System.out );
		}
	}
}
