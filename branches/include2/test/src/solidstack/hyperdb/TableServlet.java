package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.lang.Assert;
import solidstack.util.Pars;

public class TableServlet implements Servlet
{
	public void call( RequestContext context )
	{
		String database = context.getRequest().getParameter( "database" );
		String user = context.getRequest().getParameter( "user" );
		String schema = context.getRequest().getParameter( "schema" );
		String table = context.getRequest().getParameter( "table" );

		Connections connections = (Connections)context.getSession().getAttribute( "connections" );
		Connection connection = connections.getConnection( database, user );

		table = '\"' + schema + "\".\"" + table + '\"'; // TODO SQL Escaping

		try
		{
			Statement statement = connection.createStatement();
			try
			{
				final ResultSet result1 = statement.executeQuery( "SELECT COUNT(*) FROM " + table );
				Assert.isTrue( result1.next() );
				final Object object = result1.getObject( 1 );

				final ResultSet result2 = statement.executeQuery( "SELECT * FROM " + table );

				context.include( "/slt/table", new Pars( "title", "table " + table, "table", table, "result", result2, "count", object ) );
			}
			finally
			{
				statement.close();
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}
	}
}
