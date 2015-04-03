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
import org.hibernate.StatelessSession;

import solidstack.query.Query;
import solidstack.query.DataList;


/**
 * Adapts the given Query to Hibernate.
 *
 * @author René M. de Bloois
 */
public class HibernateConnectedQueryAdapter
{
	/**
	 * The query that is adapted to Hibernate.
	 */
	protected Query query;

	/**
	 * A Hibernate session.
	 */
	protected Session session;

	/**
	 * @param query A query to adapt to Hibernate.
	 * @param session A {@link Session} or a {@link StatelessSession}.
	 */
	public HibernateConnectedQueryAdapter( Query query, Object session )
	{
		this.query = query;
		if( session instanceof StatelessSession )
			this.session = new StatelessSessionAdapter( (StatelessSession)session );
		else
			this.session = (Session)session;
	}

	/**
	 * Retrieves a {@link ResultSet} from the given Hibernate {@link Session}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return a {@link ResultSet}.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#resultSet(Connection, Object)
	 */
	public ResultSet resultSet( Object args )
	{
		return HibernateSupport.resultSet( this.query, this.session, args );
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return a {@link List} of {@link Object} arrays.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#listOfArrays(Connection, Object)
	 */
	public List< Object[] > listOfArrays( Object args )
	{
		return HibernateSupport.listOfArrays( this.query, this.session, args );
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of {@link Map}s.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#dataList(Connection, Object)
	 */
	public DataList dataList( Object args )
	{
		return HibernateSupport.dataList( this.query, this.session, args );
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws JDBCException SQLExceptions are translated to JDBCExceptions by Hibernate.
	 * @see Query#updateChecked(Connection, Object)
	 */
	public int update( Object args )
	{
		return HibernateSupport.update( this.query, this.session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#list()}.
	 *
	 * @param args The arguments to the query.
	 * @return A list of Hibernate entities.
	 */
	public <T> List< T > list( Map< String, Object > args )
	{
		return HibernateSupport.list( this.query, this.session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#executeUpdate()}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The number of entities updated or deleted.
	 */
	public int executeUpdate( Object args )
	{
		return HibernateSupport.executeUpdate( this.query, this.session, args );
	}

	/**
	 * Executes {@link org.hibernate.Query#uniqueResult()}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A single Hibernate entity or null.
	 */
	public <T> T uniqueResult( Object args )
	{
		return HibernateSupport.uniqueResult( this.query, this.session, args );
	}
}
