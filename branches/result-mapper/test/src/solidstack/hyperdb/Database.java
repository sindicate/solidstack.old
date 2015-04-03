package solidstack.hyperdb;

import java.sql.Connection;
import java.util.List;
import java.util.Map;


abstract public class Database
{
	private String name;
	private String url;

	public Database( String name, String url )
	{
		this.name = name;
		this.url = url;
	}

	public String getName()
	{
		return this.name;
	}

	public String getUrl()
	{
		return this.url;
	}

	abstract public Map< String, Schema > getSchemas( Connection connection );
	abstract public List< Table > getTables( Connection connection, String schemaName );
	abstract public List< View > getViews( Connection connection, String schemaName );

	public char getIdentifierQuote()
	{
		return '"';
	}
}
