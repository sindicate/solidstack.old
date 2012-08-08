package solidstack.httpserver;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.template.TemplateNotFoundException;
import solidstack.util.Pars;

public class SltServlet implements Servlet
{
	private TemplateLoader loader;

	public SltServlet( TemplateLoader loader )
	{
		this.loader = loader;
	}

	public HttpResponse call( RequestContext context )
	{
		// TODO / should be allowed after fixing the other todo
		String url = context.getRequest().getParameter( "path" );
//		if( url.startsWith( "/" ) )
//			url = url.substring( 1 );

		try
		{
			final Template template = this.loader.getTemplate( url );
			final Pars pars = new Pars( "session", context.getSession(), "request", context.getRequest(), "args", context.getArgs() ); // TODO response
			return new HttpResponse()
			{
				@Override
				public void write( ResponseOutputStream out )
				{
					String contentType = template.getContentType();
					String charSet = null;
					if( contentType != null )
					{
						// TODO How can we send the charset if no content type is set?
						charSet = template.getCharSet();
						if( charSet != null )
							contentType += "; charset=" + charSet;
						out.setHeader( "Content-Type", contentType );
					}
					if( charSet != null )
						try
						{
							template.apply( pars, new OutputStreamWriter( out, charSet ) );
						}
						catch( UnsupportedEncodingException e )
						{
							throw new HttpException( e );
						}
					else
						template.apply( pars, new OutputStreamWriter( out ) );
				}
			};
		}
		catch( TemplateNotFoundException e )
		{
			return new StatusResponse( 404, "Not found" );
		}

//		url = url.replaceAll( "[\\\\/]", "." );
//		url = url.replaceAll( "[\\.-]", "_" );
//		request.applicationContext.callJsp( url, request );
	}
}
