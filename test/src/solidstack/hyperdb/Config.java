package solidstack.hyperdb;

import java.util.LinkedHashMap;
import java.util.Map;

public class Config
{
	static private Map< String, Database > databases;

	static
	{
		databases = new LinkedHashMap<String, Database>();
		databases.put( "TAXI", new Database( "TAXI", "jdbc:oracle:thin:@192.168.0.109:1521:XE" ) );
	}

	static public Map< String, Database > getDatabases()
	{
		return databases;
	}

	public static Database getDatabase( String database )
	{
		return databases.get( database );
	}
}
