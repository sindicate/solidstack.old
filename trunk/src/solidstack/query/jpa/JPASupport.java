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

package solidstack.query.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import solidstack.Assert;
import solidstack.query.Query;
import solidstack.query.Query.PreparedSQL;


/**
 * Adds support for JPA. JPA dependencies must be kept separate from the rest.
 * 
 * @author René M. de Bloois
 */
public class JPASupport
{
	/**
	 * Executes an update (DML) or a DDL query through the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query.
	 * @return The number of entities updated or deleted.
	 * @see javax.persistence.Query#executeUpdate()
	 */
	static public int executeUpdate( Query query, EntityManager entityManager, Map< String, Object > args )
	{
		return createQuery( query, entityManager, args ).executeUpdate();
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> List< T > getResultList( Query query, EntityManager entityManager, Map< String, Object > args )
	{
		return createQuery( query, entityManager, args ).getResultList();
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @param args The arguments to the query.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> List< T > getResultList( Query query, EntityManager entityManager, Class< T > entityClass, Map< String, Object > args )
	{
		return createQuery( query, entityManager, entityClass, args ).getResultList();
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> T getSingleResult( Query query, EntityManager entityManager, Map< String, Object > args )
	{
		return (T)createQuery( query, entityManager, args ).getSingleResult();
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @param args The arguments to the query.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> T getSingleResult( Query query, EntityManager entityManager, Class< T > entityClass, Map< String, Object > args )
	{
		return (T)createQuery( query, entityManager, entityClass, args ).getSingleResult();
	}

	/**
	 * Creates a JPA query.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @param args The arguments to the query.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	static public javax.persistence.Query createQuery( Query query, EntityManager entityManager, Class< ? > entityClass, Map< String, Object > args )
	{
		return createQuery0( query, entityManager, entityClass, args );
	}

	/**
	 * Creates a JPA query.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	static public javax.persistence.Query createQuery( Query query, EntityManager entityManager, Map< String, Object > args )
	{
		return createQuery0( query, entityManager, null, args );
	}

	/**
	 * Creates a JPA query.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @param args The arguments to the query.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	// TODO What about non native queries? And should we then rename this to createNativeQuery?
	static private javax.persistence.Query createQuery0( Query query, EntityManager entityManager, Class< ? > entityClass, Map< String, Object > args )
	{
		PreparedSQL preparedSql = query.getPreparedSQL( args );
		List< Object > pars = preparedSql.getParameters();

		javax.persistence.Query result;
		if( entityClass != null )
			result = entityManager.createNativeQuery( preparedSql.getSQL(), entityClass );
		else
			result = entityManager.createNativeQuery( preparedSql.getSQL() );
		int i = 0;
		for( Object par : pars )
		{
			if( par != null )
			{
				Assert.isFalse( par instanceof Collection );
				Assert.isFalse( par.getClass().isArray() );
			}
			result.setParameter( ++i, par );
		}
		return result;
	}
}
