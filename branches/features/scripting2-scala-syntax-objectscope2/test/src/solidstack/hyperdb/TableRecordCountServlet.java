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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Servlet;
import solidstack.lang.Assert;


public class TableRecordCountServlet implements Servlet
{
	public void call( RequestContext context )
	{
		boolean complete = false;
		try
		{
			String schema = context.getRequest().getParameter( "schema" );
			String table = context.getRequest().getParameter( "table" );
			table = '\"' + schema + "\".\"" + table + '\"'; // TODO SQL Escaping

			Connection connection = DataSource.getConnection();
			try
			{
				Statement statement = connection.createStatement();
				try
				{
					final ResultSet result1 = statement.executeQuery( "SELECT COUNT(*) FROM " + table );
					Assert.isTrue( result1.next() );
					final Object object = result1.getObject( 1 );
					context.getResponse().getWriter().write( object );
					complete = true;
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
			finally
			{
				// TODO What if the connection has been broken?: java.sql.SQLException: Closed Connection
				DataSource.release( connection );
			}
		}
		finally
		{
			if( !complete )
				context.getResponse().getWriter().write( "#ERROR#" );
		}

//		try
//		{
//			Thread.currentThread().sleep( 1000 );
//		}
//		catch( InterruptedException e )
//		{
//		}
	}
}
