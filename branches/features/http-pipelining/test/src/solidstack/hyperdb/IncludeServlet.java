package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Response;
import solidstack.httpserver.Servlet;

public class IncludeServlet implements Servlet
{
	private String path;

	public IncludeServlet( String path )
	{
		this.path = path;
	}

	public Response call( RequestContext context )
	{
		return context.include( this.path );
	}
}
