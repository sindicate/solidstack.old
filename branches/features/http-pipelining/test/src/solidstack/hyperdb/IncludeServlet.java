package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;

public class IncludeServlet implements Servlet
{
	private String path;

	public IncludeServlet( String path )
	{
		this.path = path;
	}

	public void call( RequestContext context )
	{
		context.include( this.path );
	}
}
