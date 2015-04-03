package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import solidstack.lang.SystemException;

public class Connections
{
	private Map<String, Map<String, Connection>> connections = new HashMap<String, Map<String, Connection>>();

	public void connect( Database database, String username, String password )
	{
		String url = database.getUrl();
		try
		{
			Connection connection = DriverManager.getConnection( url, username, password );
			Map<String, Connection> users = this.connections.get( database.getName() );
			if( users == null )
			{
				users = new HashMap<String, Connection>();
				this.connections.put( database.getName(), users );
			}
			users.put( username, connection );
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	public Connection getConnection( String database, String user )
	{
		return this.connections.get( database ).get( user );
	}
}
