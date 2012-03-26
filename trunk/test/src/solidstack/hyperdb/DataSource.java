package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import solidstack.httpserver.HttpException;


public class DataSource
{
	static private LinkedList< Connection > queue = new LinkedList< Connection >();

	static
	{
		try
		{
			Class.forName( "oracle.jdbc.OracleDriver" );
		}
		catch( ClassNotFoundException e )
		{
			throw new HttpException( e );
		}
	}

	synchronized static public Connection getConnection()
	{
		if( !queue.isEmpty() )
			return queue.removeFirst();

		try
		{
			System.out.println( "Getting new connection" );
			return DriverManager.getConnection( "jdbc:oracle:thin:@192.168.0.109:1521:XE", "TAXI", "taxi" );
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}
	}

	synchronized static public void release( Connection connection )
	{
		queue.add( connection );
	}
}
