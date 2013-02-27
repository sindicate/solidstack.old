/*--
 * Copyright 2006 Ren� M. de Bloois
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Mapper
{
	@Test
	public void testBasic() throws ClassNotFoundException, SQLException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
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

		RowList[] results = query.rowLists( connection, Pars.EMPTY );
		assertThat( results ).hasSize( 3 );

		RowList schemas = results[ 0 ];
		for( Map<String,Object> schema : schemas )
		{
			String name = (String)schema.get( "schemaname" ); // TODO Use generics here
			RowList tables = (RowList)schema.get( "tables" );
			int count = tableCounts.get( name );
			assertThat( tables.size() ).isEqualTo( count );
		}

		RowList tables = results[ 1 ];
		for( Map<String,Object> table : tables )
		{
			String name = (String)table.get( "tablename" ); // TODO Use generics here
			RowList columns = (RowList)table.get( "columns" );
			int count = columnCounts.get( name );
			assertThat( columns.size() ).isEqualTo( count );
			Row schema = (Row)table.get( "schema" );
			assertThat( schema ).isNotNull();
		}

		RowList columns = results[ 2 ];
		for( Map<String,Object> column : columns )
		{
			Row table = (Row)column.get( "table" );
			assertThat( table ).isNotNull();
		}
	}
}