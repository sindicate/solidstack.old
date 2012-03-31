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
		String table = context.getRequest().getParameter( "tablename" );

		Connection connection = DataSource.getConnection();
		try
		{
			Statement statement = connection.createStatement();
			try
			{
				final ResultSet result1 = statement.executeQuery( "SELECT COUNT(*) FROM " + table );
				Assert.isTrue( result1.next() );
				final Object object = result1.getObject( 1 );

				final ResultSet result2 = statement.executeQuery( "SELECT * FROM " + table );

//				new TemplateServlet().call( context, new Parameters( params ).
//						put( "title", "table " + table ).
//						put( "result", result2 ).
//						put( "count", object.toString() ).
//						put( "body", new TableViewServlet() ) );

				context = new RequestContext( context, new Pars( "title", "table " + table, "result", result2, "count", object ) );

				context.getApplication().dispatch( context );
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
		finally
		{
			// TODO What if the connection has been broken?: java.sql.SQLException: Closed Connection
			DataSource.release( connection );
		}
	}
}
