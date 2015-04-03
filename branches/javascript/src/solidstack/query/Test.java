package solidstack.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import solidstack.util.Pars;


public class Test
{
	public static void main( String[] args ) throws ClassNotFoundException, SQLException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );
		queries.setDefaultLanguage( "javascript" );

		Query query = queries.getQuery( "jartest" );
		ResultSet result = query.resultSet( connection, Pars.EMPTY );
	}
}
