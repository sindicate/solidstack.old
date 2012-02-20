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

import org.hibernate.JDBCException;
import org.hibernate.Session;

import solidstack.query.Query;


/**
 * Adapts the given Query to Hibernate.
 * 
 * @author René M. de Bloois
 */
// FIXME What about a ConnectedHibernateAdapter?
/*
 	Query(Connection,Args)
	Query+Connection(Args)
 */
public class HibernateQueryAdapter
{
	/**
	 * The query that is adapted to Hibernate.
	 */
	protected Query query;


	/**
	 * Constructor.
	 * 
	 * @param query A query to adapt to Hibernate.
	 */
	public HibernateQueryAdapter( Query query )
	{
		this.query = query;
	}

	/**
	 * Retrieves a {@link ResultSet} from the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return a {@link ResultSet}.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#resultSet(Connection, Map)
	 */
	public ResultSet resultSet( Session session, Map< String, Object > args )
	{
		return HibernateSupport.resultSet( this.query, session, args );
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return a {@link List} of {@link Object} arrays.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#listOfArrays(Connection, Map)
	 */
	public List< Object[] > listOfArrays( final Session session, Map< String, Object > args )
	{
		return HibernateSupport.listOfArrays( this.query, session, args );
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return A {@link List} of {@link Map}s.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#listOfMaps(Connection, Map)
	 */
	public List< Map< String, Object > > listOfMaps( final Session session, Map< String, Object > args )
	{
		return HibernateSupport.listOfMaps( this.query, session, args );
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#updateChecked(Connection, Map)
	 */
	public int update( Session session, Map< String, Object > args )
	{
		return HibernateSupport.update( this.query, session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#list()}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return A list of Hibernate entities.
	 */
	public <T> List< T > list( Session session, Map< String, Object > args )
	{
		return HibernateSupport.list( this.query, session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#executeUpdate()}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return The number of entities updated or deleted.
	 */
	public int executeUpdate( Session session, Map< String, Object > args )
	{
		return HibernateSupport.executeUpdate( this.query, session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#uniqueResult()}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @param args The arguments to the query.
	 * @return A single Hibernate entity or null.
	 */
	public <T> T uniqueResult( Session session, Map< String, Object > args )
	{
		return HibernateSupport.uniqueResult( this.query, session, args );
	}
}
