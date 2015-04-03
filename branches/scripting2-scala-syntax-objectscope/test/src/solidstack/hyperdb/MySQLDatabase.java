/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.hyperdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import solidstack.httpserver.HttpException;
import solidstack.query.Query;
import solidstack.query.QueryLoader;
import solidstack.util.Pars;


public class MySQLDatabase extends Database
{
//	static protected Map< String, Schema > schemaCache;
	static public final QueryLoader queries;

	static
	{
		queries = new QueryLoader();
		queries.setDefaultLanguage( "groovy" );
		queries.setReloading( true );
		queries.setTemplatePath( "classpath:/solidstack/hyperdb/mysql" );
	}

	public MySQLDatabase( String name, String url )
	{
		super( name, url );
	}

	@Override
	public char getIdentifierQuote()
	{
		return '`';
	}

	@Override
	synchronized public Map< String, Schema > getSchemas( Connection connection )
	{
//		if( schemaCache != null )
//			return schemaCache;

		Query query = queries.getQuery( "selectSchemas.sql" );

		Map< String, Schema > schemas = new LinkedHashMap< String, Schema >();
//		Connection connection = DataSource.getConnection();
//		try
//		{
			List<Object[]> users = query.listOfArrays( connection, Pars.EMPTY );
			for( Object[] user : users )
			{
				String name = (String)user[ 0 ];
//				BigDecimal tables = (BigDecimal)user[ 1 ];
//				BigDecimal views = (BigDecimal)user[ 2 ];
				schemas.put( name, new Schema( name, 0, 0 ) );
			}
//			schemaCache = schemas;
//		}
//		finally
//		{
//			DataSource.release( connection );
//		}

		return schemas;
	}

	@Override
	synchronized public List< Table > getTables( Connection connection, String schemaName )
	{
		String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES ORDER BY TABLE_NAME";

		List< Table > tables = new ArrayList< Table >();
		try
		{
			PreparedStatement statement = connection.prepareStatement( sql );
			try
			{
//				statement.setString( 1, schemaName );
				ResultSet result = statement.executeQuery();
				while( result.next() )
					tables.add( new Table( result.getString( 1 ), result.getString( 2 ), result.getLong( 3 ) ) );
			}
			finally
			{
				statement.close();
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}

		return tables;
	}

	@Override
	synchronized  public List< View > getViews( Connection connection, String schemaName )
	{
		String sql = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = ? ORDER BY VIEW_NAME";

		List< View > views = new ArrayList< View >();
		try
		{
			PreparedStatement statement = connection.prepareStatement( sql );
			try
			{
				statement.setString( 1, schemaName );
				ResultSet result = statement.executeQuery();
				while( result.next() )
					views.add( new View( result.getString( 1 ) ) );
			}
			finally
			{
				statement.close();
			}
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}

		return views;
	}
}
