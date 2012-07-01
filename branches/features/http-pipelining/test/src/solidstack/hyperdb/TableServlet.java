package solidstack.hyperdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Response;
import solidstack.httpserver.ResponseOutputStream;
import solidstack.httpserver.Servlet;
import solidstack.lang.Assert;
import solidstack.util.Pars;

public class TableServlet implements Servlet
{
	public Response call( final RequestContext context )
	{
		final String database = context.getRequest().getParameter( "database" );
		final String user = context.getRequest().getParameter( "user" );
		final String schema = context.getRequest().getParameter( "schema" );
		final String table = context.getRequest().getParameter( "table" );

		return new Response()
		{
			@Override
			public void write( ResponseOutputStream out ) throws IOException
			{
				Connections connections = (Connections)context.getSession().getAttribute( "connections" );
				ConnectionHolder holder = connections.getConnection( database, user );
				Connection connection = holder.getConnection();
				char identifierQuote = holder.getDatabase().getIdentifierQuote();

				final String theTable = identifierQuote + schema + identifierQuote + "." + identifierQuote + table + identifierQuote; // TODO SQL Escaping

				try
				{
					Statement statement = connection.createStatement();
					try
					{
						final ResultSet result1 = statement.executeQuery( "SELECT COUNT(*) FROM " + theTable );
						Assert.isTrue( result1.next() );
						final Object object = result1.getObject( 1 );

						final ResultSet result2 = statement.executeQuery( "SELECT * FROM " + theTable );

						context.include( "/slt/table", new Pars( "title", "table " + theTable, "table", theTable, "result", result2, "count", object ) );
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
		};
	}
}
