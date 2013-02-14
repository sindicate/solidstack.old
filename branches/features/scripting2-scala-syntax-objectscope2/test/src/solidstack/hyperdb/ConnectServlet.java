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

import solidstack.httpserver.Request;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.httpserver.Session;

public class ConnectServlet implements Servlet
{
	public void call( RequestContext context )
	{
		Request request = context.getRequest();
		if( request.getMethod().equals( "GET" ) )
		{
			context.include( "/slt/connect" );
			return;
		}

		String databaseName = request.getParameter( "database" );
		String username = request.getParameter( "username" );
		String password = request.getParameter( "password" );

		Database database = Config.getDatabase( databaseName );

		Session session = context.getSession();
		Connections connections = (Connections)session.getAttribute( "connections" );
		if( connections == null )
		{
			connections = new Connections();
			session.setAttribute( "connections", connections );
		}
		connections.connect( database, username, password );

		context.redirect( "/databases/" + databaseName + '/' + username + "/schemas" );
	}
}
