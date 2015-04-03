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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import solidstack.httpserver.HttpException;


public class DataSource
{
	static private LinkedList< Connection > queue = new LinkedList< Connection >();

	static
	{
		try
		{
			Class.forName( "oracle.jdbc.OracleDriver" );
		}
		catch( ClassNotFoundException e )
		{
			throw new HttpException( e );
		}
	}

	synchronized static public Connection getConnection()
	{
		if( !queue.isEmpty() )
		{
			Connection connection = queue.removeFirst();
			try
			{
				connection.createStatement().executeQuery( "SELECT * FROM DUAL" );
				return connection;
			}
			catch( SQLException e )
			{
				try
				{
					connection.close();
				}
				catch( SQLException e1 )
				{
					// Ignore
				}
			}
		}

		try
		{
			System.out.println( "Getting new connection" );


//			return DriverManager.getConnection( "jdbc:oracle:thin:@172.31.1.26:1521:OLTPS1", "JONGSA", "56287" );
			return DriverManager.getConnection( "jdbc:oracle:thin:@172.15.28.87:1521:ECHDWHP", "BCON_RAPPORTAGE", "BCON_RAPPORTAGE" );
		}
		catch( SQLException e )
		{
			throw new HttpException( e );
		}
	}

	synchronized static public void release( Connection connection )
	{
		queue.add( connection );
	}
}
