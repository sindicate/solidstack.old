package solidstack.nio;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseWriter;
import solidstack.httpserver.Servlet;


public class BackEndRootServlet implements Servlet
{
	public void call( RequestContext context )
	{
		context.getResponse().setContentType( "text/html", null );
		ResponseWriter writer = context.getResponse().getWriter();
		writer.write( "Hello World!\n" );
	}
}
