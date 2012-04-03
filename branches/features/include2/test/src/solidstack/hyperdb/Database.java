package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import solidstack.httpserver.HttpException;
import solidstack.lang.Assert;


public class Database
{
	static protected Map< String, Schema > schemaCache;

	private String url;

	public Database( String url )
	{
		this.url = url;
	}

	public String getUrl()
	{
		return this.url;
	}

	synchronized static public Map< String, Schema > getSchemas()
	{
		if( schemaCache != null )
			return schemaCache;

		String sql = "SELECT TABLES.OWNER, COALESCE( TABLES.COUNT, 0 ) TABLES, COALESCE( VIEWS.COUNT, 0 ) VIEWS\n" +
				"FROM ( SELECT OWNER, COUNT(*) COUNT FROM ALL_TABLES GROUP BY OWNER ) TABLES\n" +
				"FULL OUTER JOIN ( SELECT OWNER, COUNT(*) COUNT FROM ALL_VIEWS GROUP BY OWNER ) VIEWS\n" +
				"ON VIEWS.OWNER = TABLES.OWNER\n" +
				"ORDER BY TABLES.OWNER";

		Map< String, Schema > schemas = new LinkedHashMap< String, Schema >();
		try
		{
			Connection connection = DataSource.getConnection();
			try
			{
				Statement statement = connection.createStatement();
				try
				{
					ResultSet result = statement.executeQuery( sql );
					while( result.next() )
					{
						String name = result.getString( 1 );
						int tables = result.getInt( 2 );
						int views = result.getInt( 3 );
						schemas.put( name, new Schema( name, tables, views ) );
					}

					schemaCache = schemas;
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				DataSource.release( connection );
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}

		return schemas;
	}

	synchronized static public List< Table > getTables( String schemaName )
	{
		Schema schema = getSchemas().get( schemaName );
		Assert.notNull( schema, "Schema not found" );

		if( schema.getTables() != null )
			return schema.getTables();

		String sql = "SELECT TABLE_NAME, NUM_ROWS FROM ALL_TABLES WHERE OWNER = ? ORDER BY TABLE_NAME";

		List< Table > tables = new ArrayList< Table >();
		try
		{
			Connection connection = DataSource.getConnection();
			try
			{
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, schemaName );
					ResultSet result = statement.executeQuery();
					while( result.next() )
						tables.add( new Table( result.getString( 1 ), result.getLong( 2 ) ) );

					schema.setTables( tables );
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				DataSource.release( connection );
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}

		return tables;
	}

	synchronized static public List< View > getViews( String schemaName )
	{
		Schema schema = getSchemas().get( schemaName );
		Assert.notNull( schema, "Schema not found" );

		if( schema.getViews() != null )
			return schema.getViews();

		String sql = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = ? ORDER BY VIEW_NAME";

		List< View > views = new ArrayList< View >();
		try
		{
			Connection connection = DataSource.getConnection();
			try
			{
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, schemaName );
					ResultSet result = statement.executeQuery();
					while( result.next() )
						views.add( new View( result.getString( 1 ) ) );

					schema.setViews( views );
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				DataSource.release( connection );
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}

		return views;
	}
}
