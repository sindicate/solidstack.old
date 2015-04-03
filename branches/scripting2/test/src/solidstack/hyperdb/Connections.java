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
import java.util.HashMap;
import java.util.Map;

import solidstack.lang.SystemException;

public class Connections
{
	private Map<String, Map<String, ConnectionHolder>> connections = new HashMap<String, Map<String, ConnectionHolder>>();

	public void connect( Database database, String username, String password )
	{
		String url = database.getUrl();
		try
		{
			Connection connection = DriverManager.getConnection( url, username, password );
			ConnectionHolder holder = new ConnectionHolder( connection, database );
			Map<String, ConnectionHolder> users = this.connections.get( database.getName() );
			if( users == null )
			{
				users = new HashMap<String, ConnectionHolder>();
				this.connections.put( database.getName(), users );
			}
			users.put( username, holder );
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	public ConnectionHolder getConnection( String database, String user )
	{
		return this.connections.get( database ).get( user );
	}
}
