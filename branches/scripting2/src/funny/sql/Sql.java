package funny.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import solidstack.query.Query;
import solidstack.query.ResultList;
import solidstack.script.java.Function;

public class Sql
{
	private String url;

	public Sql( String driver, String url ) throws ClassNotFoundException, SQLException
	{
		Class.forName( driver );
		this.url = url;
	}

	public void eachResult( String query, Function function ) throws SQLException
	{
		Connection connection = DriverManager.getConnection( this.url );
		try
		{
			ResultSet result = connection.createStatement().executeQuery( query );
			while( result.next() )
				function.call( result );
		}
		finally
		{
			connection.close();
		}
	}

	public List list( String query ) throws SQLException
	{
		Connection connection = DriverManager.getConnection( this.url );
		try
		{
			ResultSet resultSet = connection.createStatement().executeQuery( query );
			return new ResultList( Query.listOfArrays( resultSet, true ), Query.getColumnLabelMap( resultSet.getMetaData() ) );
		}
		finally
		{
			connection.close();
		}
	}
}
