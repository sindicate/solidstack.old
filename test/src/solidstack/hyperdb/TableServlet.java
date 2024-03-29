/*--
 * Copyright 2012 Ren� M. de Bloois
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.lang.Assert;
import solidstack.util.Pars;

public class TableServlet implements Servlet
{
	public void call( RequestContext context )
	{
		String database = context.getRequest().getParameter( "database" );
		String user = context.getRequest().getParameter( "user" );
		String schema = context.getRequest().getParameter( "schema" );
		String table = context.getRequest().getParameter( "table" );

		Connections connections = (Connections)context.getSession().getAttribute( "connections" );
		ConnectionHolder holder = connections.getConnection( database, user );
		Connection connection = holder.getConnection();
		char identifierQuote = holder.getDatabase().getIdentifierQuote();

		table = identifierQuote + schema + identifierQuote + "." + identifierQuote + table + identifierQuote; // TODO SQL Escaping

		try
		{
			Statement statement = connection.createStatement();
			try
			{
				final ResultSet result1 = statement.executeQuery( "SELECT COUNT(*) FROM " + table );
				Assert.isTrue( result1.next() );
				final Object object = result1.getObject( 1 );

				final ResultSet result2 = statement.executeQuery( "SELECT * FROM " + table );

				context.include( "/slt/table", new Pars( "title", "table " + table, "table", table, "result", result2, "count", object ) );
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
	}
}
