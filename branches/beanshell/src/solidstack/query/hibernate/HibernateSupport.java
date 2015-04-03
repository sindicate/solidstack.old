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
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import solidstack.query.Query;
import solidstack.query.ResultHolder;


/**
 * 
 * @author René M. de Bloois
 */
public class HibernateSupport
{
	/**
	 * Retrieves a {@link ResultSet} from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return a {@link ResultSet}.
	 * @see Query#resultSet()
	 */
	static public ResultSet resultSet( final Query query, Session session, final Map< String, Object > args )
	{
		final ResultHolder< ResultSet > result = new ResultHolder< ResultSet >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.resultSet( connection, args ) );
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return a {@link List} of {@link Object} arrays.
	 */
	static public List< Object[] > listOfArrays( final Query query, final Session session, final Map< String, Object > args )
	{
		final ResultHolder< List< Object[] > > result = new ResultHolder< List< Object[] > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.listOfArrays( connection, args ) );
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return A {@link List} of {@link Map}s.
	 */
	static public List< Map< String, Object > > listOfMaps( final Query query, final Session session, final Map< String, Object > args )
	{
		final ResultHolder< List< Map< String, Object > > > result = new ResultHolder< List< Map< String, Object > > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.listOfMaps( connection, args ) );
			}
		});

		return result.get();
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws HibernateException SQLExceptions are translated to HibernateExceptions by Hibernate.
	 */
	static public int update( final Query query, Session session, final Map< String, Object > args )
	{
		final ResultHolder< Integer > result = new ResultHolder< Integer >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.update( connection, args ) );
			}
		});

		return result.get();
	}
}
