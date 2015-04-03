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

package solidstack.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import solidstack.util.Pars;


/**
 * Main class to test the jar.
 *
 * @author René de Bloois
 */
public class Test
{
	/**
	 * Main method to test the jar.
	 *
	 * @param args Arguments.
	 */
	public static void main( String[] args )
	{
		try
		{
			Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		}
		catch( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		Connection connection;
		try
		{
			connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

			QueryLoader queries = new QueryLoader();
			queries.setTemplatePath( "classpath:/solidstack/query" );
			queries.setDefaultLanguage( "javascript" );

			Query query = queries.getQuery( "jartest" );
			query.resultSet( connection, Pars.EMPTY );
		}
		catch( SQLException e )
		{
			e.printStackTrace();
		}
	}
}
