/*--
 * Copyright 2006 René M. de Bloois
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

package solidstack.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;


public class Basic
{
	@Test
	public void testBasic() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		CompiledQuery compiledQuery = queries.getQuery( "test" );

		Map< String, Object > params = new HashMap< String, Object >();
		Query query = compiledQuery.params( params );
		List< Map< String, Object > > result = query.list( connection, true );
		assert result.size() == 22;
		for( Map< String, Object > row : result )
			System.out.println( row.get( "tablename" ) );

		params.put( "prefix", "SYST" );
		query = compiledQuery.params( params );
		result = query.list( connection, true );
		assert result.size() == 3;
		for( Map< String, Object > row : result )
			System.out.println( row.get( "tablename" ) );

		params.clear();
		params.put( "name", "SYSTABLES" );
		query = compiledQuery.params( params );
		result = query.list( connection, true );
		assert result.size() == 1;
		for( Map< String, Object > row : result )
			System.out.println( row.get( "tablename" ) );

		params.put( "name", "SYSTABLES" );
		params.put( "prefix", "SYST" );
		query = compiledQuery.params( params );
		result = query.list( connection, true );
		assert result.size() == 1;
		for( Map< String, Object > row : result )
			System.out.println( row.get( "tablename" ) );

		params.clear();
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		query = compiledQuery.params( params );
		result = query.list( connection, true );
		assert result.size() == 2;
		for( Map< String, Object > row : result )
			System.out.println( row.get( "tablename" ) );
	}
}
