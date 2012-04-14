package solidstack.hyperdb;

import solidstack.httpserver.Request;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.httpserver.Session;

public class ConnectServlet implements Servlet
{
	public void call( RequestContext context )
	{
		Request request = context.getRequest();
		if( request.getMethod().equals( "GET" ) )
		{
			context.include( "/slt/connect" );
			return;
		}

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

		context.redirect( "/databases/" + databaseName + '/' + username + "/schemas" );
	}
}
