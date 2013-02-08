package funny.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import solidstack.query.Query;
import solidstack.query.ResultList;
import solidstack.script.java.Function;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.PString;


// TODO Integrate with the query template package
public class JDBC
{
	private String url;
	private Connection connection;

	static public JDBC apply( String driver, String url ) throws ClassNotFoundException, SQLException
	{
		return new JDBC( driver, url );
	}

	static public JDBCMonad doWith( String driver, String url ) throws ClassNotFoundException, SQLException
	{
		Class.forName( driver );
		return new JDBCMonad( url );
	}

	private JDBC( String driver, String url ) throws ClassNotFoundException, SQLException
	{
		Class.forName( driver );
		connect( url );
	}

	private JDBC( String url ) throws ClassNotFoundException, SQLException
	{
		connect( url );
	}

	private void connect( String url ) throws SQLException
	{
		this.url = url;
		this.connection = DriverManager.getConnection( this.url );
	}

	public void close() throws SQLException
	{
		this.connection.close();
	}

	public void eachRow( String query, Function function ) throws SQLException
	{
		ResultSet result = query( query );
		while( result.next() )
			function.call( result );
	}

	public ResultSet query( String query ) throws SQLException
	{
		return this.connection.createStatement().executeQuery( query );
	}

	// TODO Do this with the query template package
	public ResultSet query( PString query ) throws SQLException
	{
		Object[] values = query.getValues();
		int len = values.length;
		Object[] parameters = new Object[ len ];
		System.arraycopy( values, 0, parameters, 0, len );
		for( int i = 0; i < len; i++ )
			values[ i ] = "?";
		System.out.println( query.toString() );
		PreparedStatement statement = this.connection.prepareStatement( query.toString() );
		for( int i = 0; i < len; i++ )
			statement.setObject( i + 1, parameters[ i ] );
		return statement.executeQuery();
	}

	public List list( String query ) throws SQLException
	{
		ResultSet resultSet = query( query );
		return new ResultList( Query.listOfArrays( resultSet, true ), Query.getColumnLabelMap( resultSet.getMetaData() ) );
	}

	public boolean execute( String query ) throws SQLException
	{
		return this.connection.createStatement().execute( query );
	}

	public int update( String query ) throws SQLException
	{
		return this.connection.createStatement().executeUpdate( query );
	}

	static class JDBCMonad
	{
		private String url;

		public JDBCMonad( String url )
		{
			this.url = url;
		}

		public Object apply( FunctionObject function ) throws ClassNotFoundException, SQLException
		{
			JDBC jdbc = new JDBC( this.url );
			try
			{
				return function.call( jdbc );
			}
			finally
			{
				jdbc.close();
			}
		}
	}
}
