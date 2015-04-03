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

package solidstack.query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;


/**
 * 
 * @author René M. de Bloois
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
	 * @return a {@link ResultSet}.
	 * @see Query#resultSet()
	 */
	public ResultSet resultSet( Session session )
	{
		return HibernateSupport.resultSet( this.query, session );
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @return a {@link List} of {@link Object} arrays.
	 */
	public List< Object[] > listOfArrays( final Session session )
	{
		return HibernateSupport.listOfArrays( this.query, session );
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @return A {@link List} of {@link Map}s.
	 */
	public List< Map< String, Object > > listOfMaps( final Session session )
	{
		return HibernateSupport.listOfMaps( this.query, session );
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 * 
	 * @param session The Hibernate {@link Session} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws HibernateException SQLExceptions are translated to HibernateExceptions by Hibernate.
	 */
	public int update( Session session )
	{
		return HibernateSupport.update( this.query, session );
	}
}
