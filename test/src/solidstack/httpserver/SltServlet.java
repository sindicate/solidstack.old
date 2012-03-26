package solidstack.httpserver;

import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.util.Pars;

public class SltServlet implements Servlet
{
	private TemplateLoader loader;

	public SltServlet( TemplateLoader loader )
	{
		this.loader = loader;
	}

	public void call( RequestContext request, Pars params )
	{
		// TODO / should be allowed after fixing the other todo
		String url = request.getRequest().getParameter( "path" );
		if( url.startsWith( "/" ) )
			url = url.substring( 1 );

		Template template = this.loader.getTemplate( url );
		template.apply( params, request.getResponse().getWriter() );

//		url = url.replaceAll( "[\\\\/]", "." );
//		url = url.replaceAll( "[\\.-]", "_" );
//		request.applicationContext.callJsp( url, request );
	}
}
