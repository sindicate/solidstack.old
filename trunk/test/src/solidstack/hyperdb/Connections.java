package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import solidstack.lang.SystemException;

public class Connections
{
	private Map<String, Map<String, ConnectionHolder>> connections = new HashMap<String, Map<String, ConnectionHolder>>();

	public void connect( Database database, String username, String password )
	{
		String url = database.getUrl();
		try
		{
			Connection connection = DriverManager.getConnection( url, username, password );
			ConnectionHolder holder = new ConnectionHolder( connection, database );
			Map<String, ConnectionHolder> users = this.connections.get( database.getName() );
			if( users == null )
			{
				users = new HashMap<String, ConnectionHolder>();
				this.connections.put( database.getName(), users );
			}
			users.put( username, holder );
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	public ConnectionHolder getConnection( String database, String user )
	{
		return this.connections.get( database ).get( user );
	}
}
