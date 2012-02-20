/*--
 * Copyright 2011 René M. de Bloois
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

package solidstack.query.hibernate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.JDBCException;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import solidstack.Assert;
import solidstack.query.Query;
import solidstack.query.Query.PreparedSQL;
import solidstack.query.Query.Type;
import solidstack.query.QuerySQLException;
import solidstack.query.ResultHolder;


/**
 * Adds support for Hibernate. Hibernate dependencies must be kept separate from the rest.
 * 
 * @author René M. de Bloois
 */
// TODO What about HQL query templates?
public class HibernateSupport
{
	/**
	 * Retrieves a {@link ResultSet} from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return a {@link ResultSet}.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#resultSet(Connection, Map)
	 */
	static public ResultSet resultSet( final Query query, Session session, final Map< String, Object > args )
	{
		final ResultHolder< ResultSet > result = new ResultHolder< ResultSet >();

		session.doWork( new Work()
		{
			public void execute( Connection connection ) throws SQLException
			{
				try
				{
					result.set( query.resultSet( connection, args ) );
				}
				catch( QuerySQLException e )
				{
					throw e.getSQLException();
				}
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return a {@link List} of {@link Object} arrays.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#listOfArrays(Connection, Map)
	 */
	static public List< Object[] > listOfArrays( final Query query, final Session session, final Map< String, Object > args )
	{
		final ResultHolder< List< Object[] > > result = new ResultHolder< List< Object[] > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection ) throws SQLException
			{
				try
				{
					result.set( query.listOfArrays( connection, args ) );
				}
				catch( QuerySQLException e )
				{
					throw e.getSQLException();
				}
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return A {@link List} of {@link Map}s.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#listOfMaps(Connection, Map)
	 */
	static public List< Map< String, Object > > listOfMaps( final Query query, final Session session, final Map< String, Object > args )
	{
		final ResultHolder< List< Map< String, Object > > > result = new ResultHolder< List< Map< String, Object > > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection ) throws SQLException
			{
				try
				{
					result.set( query.listOfMaps( connection, args ) );
				}
				catch( QuerySQLException e )
				{
					throw e.getSQLException();
				}
			}
		});

		return result.get();
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#updateChecked(Connection, Map)
	 */
	static public int update( final Query query, Session session, final Map< String, Object > args )
	{
		final ResultHolder< Integer > result = new ResultHolder< Integer >();

		session.doWork( new Work()
		{
			public void execute( Connection connection ) throws SQLException
			{
				result.set( query.updateChecked( connection, args ) );
			}
		});

		return result.get();
	}

	static public <T> List<T> list( Query query, Session session, Map<String, Object> args )
	{
		return createQuery( query, session, args ).list();
	}

	static public int executeUpdate( Query query, Session session, Map<String, Object> args )
	{
		return createQuery( query, session, args ).executeUpdate();
	}

	static public <T> T uniqueResult( Query query, Session session, Map<String, Object> args )
	{
		return (T)createQuery( query, session, args ).uniqueResult();
	}

	// TODO Rename my Query to SolidQuery?
	static public org.hibernate.Query createQuery( Query query, Session session, Map< String, Object > args )
	{
		PreparedSQL preparedSql = query.getPreparedSQL( args );

		org.hibernate.Query result;
		if( query.getType() == Type.NATIVE )
			result = session.createSQLQuery( preparedSql.getSQL() );
		else if( query.getType() == Type.HQL )
			result = session.createQuery( preparedSql.getSQL() );
		else
			throw new QueryException( "Query type'" + query.getType() + "' not recognized" );

		List< Object > pars = preparedSql.getParameters();
		int i = 0;
		for( Object par : pars )
		{
			if( par != null )
			{
				Assert.isFalse( par instanceof Collection );
				Assert.isFalse( par.getClass().isArray() );
			}
			result.setParameter( i++, par );
		}
		return result;
	}
}
