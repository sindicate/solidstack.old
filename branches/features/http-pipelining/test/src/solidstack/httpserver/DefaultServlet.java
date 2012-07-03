package solidstack.httpserver;

import java.io.IOException;
import java.io.InputStream;


public class DefaultServlet implements Servlet
{
	public HttpResponse call( RequestContext context )
	{
//		Response response = context.getResponse();

		String url = context.getRequest().getUrl();
		if( url.startsWith( "/" ) )
			url = url.substring( 1 );

		// TODO Should we open in the response?
		final InputStream in = DefaultServlet.class.getClassLoader().getResourceAsStream( url );
		if( in == null )
			return new StatusResponse( 404, "Not found" );

		String e = null;
		int pos = url.lastIndexOf( '.' );
		if( pos > 0 )
			if( pos > url.lastIndexOf( '/' ) )
				e = url.substring( pos + 1 );
		final String extension = e;

		return new HttpResponse()
		{
			@Override
			public void write( ResponseOutputStream out )
			{
				if( extension.equals( "properties" ) )
					out.setContentType( "text/plain", "ISO-8859-1" );
				else if( extension.equals( "ico" ) )
					out.setContentType( "image/vnd.microsoft.icon", null );
				else if( extension.equals( "js" ) )
					out.setContentType( "text/javascript", null );
				else if( extension.equals( "css" ) )
					out.setContentType( "text/css", null );

				out.setHeader( "Cache-Control", "max-age=3600" );

				try
				{
					try
					{
						byte[] buffer = new byte[ 4096 ];
						int len = in.read( buffer );
						while( len >= 0 )
						{
							out.write( buffer, 0, len );
							len = in.read( buffer );
						}
					}
					finally
					{
						in.close();
					}
				}
				catch( IOException e )
				{
					throw new HttpException( e );
				}
			}
		};
	}
}
