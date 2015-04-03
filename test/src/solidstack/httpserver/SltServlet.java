package solidstack.httpserver;

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

	public void call( RequestContext context )
	{
		// TODO / should be allowed after fixing the other todo
		String url = context.getRequest().getParameter( "path" );
//		if( url.startsWith( "/" ) )
//			url = url.substring( 1 );

		try
		{
			Template template = this.loader.getTemplate( url );
			Pars pars = new Pars( "session", context.getSession(), "request", context.getRequest(), "args", context.getArgs() ); // TODO response
			template.apply( pars, context.getResponse().getWriter() );
		}
		catch( TemplateNotFoundException e )
		{
			context.getResponse().setStatusCode( 404, "Not found" );
		}

//		url = url.replaceAll( "[\\\\/]", "." );
//		url = url.replaceAll( "[\\.-]", "_" );
//		request.applicationContext.callJsp( url, request );
	}
}
