package solidstack.hyperdb;

import solidstack.httpserver.RedirectResponse;
import solidstack.httpserver.Request;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.HttpResponse;
import solidstack.httpserver.Servlet;
import solidstack.httpserver.Session;

public class ConnectServlet implements Servlet
{
	public HttpResponse call( RequestContext context )
	{
		Request request = context.getRequest();
		if( request.getMethod().equals( "GET" ) )
			return context.include( "/slt/connect" );

		String databaseName = request.getParameter( "database" );
		String username = request.getParameter( "username" );
		String password = request.getParameter( "password" );

		Database database = Config.getDatabase( databaseName );

		Session session = context.getSession();
		Connections connections = (Connections)session.getAttribute( "connections" );
		if( connections == null )
		{
			connections = new Connections();
			session.setAttribute( "connections", connections );
		}
		connections.connect( database, username, password );

		return new RedirectResponse( "/databases/" + databaseName + '/' + username + "/schemas" );
	}
}
