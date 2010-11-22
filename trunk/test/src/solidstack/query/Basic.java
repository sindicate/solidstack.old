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
import java.util.ArrayList;
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

		Map< String, Object > params = new HashMap< String, Object >();
		Query query = queries.bind( "test", params );
		List< Map< String, Object > > result = query.listOfRowMaps( connection, true );
		assert result.size() == 22;

		params.put( "prefix", "SYST" );
		query = queries.bind( "test", params );
		result = query.listOfRowMaps( connection, true );
		assert result.size() == 3;

		params.clear();
		params.put( "name", "SYSTABLES" );
		query = queries.bind( "test", params );
		List< Object[] > array = query.listOfObjectArrays( connection, true );
		assert array.size() == 1;

		params.put( "name", "SYSTABLES" );
		params.put( "prefix", "SYST" );
		query = queries.bind( "test", params );
		result = query.listOfRowMaps( connection, true );
		assert result.size() == 1;

		params.clear();
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		query = queries.bind( "test", params );
		result = query.listOfRowMaps( connection, true );
		assert result.size() == 2;
	}

	@Test
	public void testTransform() throws Exception
	{
		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
//		params.put( "name", "SYSTABLES" );
		params.put( "prefix", "SYST" );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.bind( "test", params );
		List< Object > pars = new ArrayList< Object >();
		String sql = query.getPreparedSQL( pars );

		assert sql.equals( "	SELECT *\n" +
				"	FROM SYS.SYSTABLES\n" +
				"	WHERE 1 = 1\n" +
				"	AND TABLENAME LIKE 'SYST%'\n" +
		"	AND TABLENAME IN (?,?)\n" );

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test
	public void testInJar() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
		Query query = queries.bind( "test2", params );
		List< Map< String, Object > > result = query.listOfRowMaps( connection, true );
		assert result.size() == 22;
	}
}
