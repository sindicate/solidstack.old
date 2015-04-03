package solidstack.hyperdb;

import java.sql.Connection;

public class ConnectionHolder
{
	private Connection connection;
	private Database database;

	public ConnectionHolder( Connection connection, Database database )
	{
		this.connection = connection;
		this.database = database;
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	public Database getDatabase()
	{
		return this.database;
	}
}
