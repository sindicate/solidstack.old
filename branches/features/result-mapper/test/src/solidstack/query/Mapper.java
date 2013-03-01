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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Mapper
{
	static boolean init;

	@BeforeTest
	static public void init() throws ClassNotFoundException
	{
		assert !init;
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
	}

	@Test
	public void testBasic() throws SQLException, IOException
	{
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		Statement stat = connection.createStatement(); // TODO Can't we do this with a Query?

		ResultSet result = stat.executeQuery( "SELECT S.SCHEMANAME, COUNT(*) FROM SYS.SYSSCHEMAS S JOIN SYS.SYSTABLES T ON T.SCHEMAID = S.SCHEMAID GROUP BY S.SCHEMANAME" );
		Map<String,Integer> tableCounts = new HashMap<String,Integer>();
		while( result.next() )
			tableCounts.put( result.getString( 1 ), result.getInt( 2 ) );

		result = stat.executeQuery( "SELECT T.TABLENAME, COUNT(*) FROM SYS.SYSTABLES T JOIN SYS.SYSCOLUMNS C ON C.REFERENCEID = T.TABLEID GROUP BY T.TABLENAME" );
		Map<String,Integer> columnCounts = new HashMap<String,Integer>();
		while( result.next() )
			columnCounts.put( result.getString( 1 ), result.getInt( 2 ) );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Query query = queries.getQuery( "mapper.sql" );

		DataList[] results = query.dataLists( connection, Pars.EMPTY );
		assertThat( results ).hasSize( 3 );

		DataList schemas = results[ 0 ];
//		Writer w = new FileWriter( "schemas.html" );
//		try
//		{
//			schemas.writeAsHTML( w );
//		}
//		finally
//		{
//			w.close();
//		}
		for( Map<String,Object> schema : schemas )
		{
			String name = (String)schema.get( "schemaname" ); // TODO Use generics here
			DataList tables = (DataList)schema.get( "tables" );
			int count = tableCounts.get( name );
			assertThat( tables.size() ).isEqualTo( count );
		}

		DataList tables = results[ 1 ];
		for( Map<String,Object> table : tables )
		{
			String name = (String)table.get( "tablename" ); // TODO Use generics here
			DataList columns = (DataList)table.get( "columns" );
			int count = columnCounts.get( name );
			assertThat( columns.size() ).isEqualTo( count );
			DataObject schema = (DataObject)table.get( "schema" );
			assertThat( schema ).isNotNull();
		}

		DataList columns = results[ 2 ];
		for( Map<String,Object> column : columns )
		{
			DataObject table = (DataObject)column.get( "table" );
			assertThat( table ).isNotNull();
		}
	}

	@Test
	public void testRollup() throws SQLException, IOException
	{
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Query query = queries.getQuery( "mapper-rollup.sql" );

		DataList results = query.dataList( connection, Pars.EMPTY );
//		Writer w = new FileWriter( "rowlist.html" );
//		try
//		{
//			results.writeAsHTML( w );
//		}
//		finally
//		{
//			w.close();
//		}
		assertThat( results.size() ).isEqualTo( 164 );
	}

	@Test
	public void testFilter() throws SQLException, IOException
	{
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Query query = queries.getQuery( "mapper-filter.sql" );

		DataList results = query.dataList( connection, Pars.EMPTY );
//		Writer w = new FileWriter( "filter.html" );
//		try
//		{
//			results.writeAsHTML( w );
//		}
//		finally
//		{
//			w.close();
//		}
		assertThat( results.size() ).isGreaterThanOrEqualTo( 2 );
	}
}
