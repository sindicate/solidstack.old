package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.HttpResponse;
import solidstack.httpserver.Servlet;

public class IncludeServlet implements Servlet
{
	private String path;

	public IncludeServlet( String path )
	{
		this.path = path;
	}

	public HttpResponse call( RequestContext context )
	{
		return context.include( this.path );
	}
}
