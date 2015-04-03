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

import java.util.List;

import javax.persistence.EntityManager;

import solidstack.query.Query;
import solidstack.query.hibernate.HibernateConnectedQueryAdapter;


/**
 * Adapts the given Query to JPA.
 *
 * @author René M. de Bloois
 */
public class JPAConnectedQueryAdapter
{
	/**
	 * The query that is adapted to JPA.
	 */
	protected Query query;

	/**
	 * A JPA entity manager.
	 */
	protected EntityManager entityManager;


	/**
	 * Constructor.
	 *
	 * @param query A query to adapt to JPA.
	 * @param entityManager A {@link javax.persistence.EntityManager}.
	 */
	public JPAConnectedQueryAdapter( Query query, Object entityManager )
	{
		this.query = query;
		this.entityManager = (EntityManager)entityManager;
	}

	/**
	 * Executes an update (DML) or a DDL query through the given {@link EntityManager}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The number of entities updated or deleted.
	 * @see javax.persistence.Query#executeUpdate()
	 */
	public int executeUpdate( Object args )
	{
		return JPASupport.executeUpdate( this.query, this.entityManager, args );
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	public <T> List<T> getResultList( Object args )
	{
		return JPASupport.getResultList( this.query, this.entityManager, args );
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 *
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	public <T> List<T> getResultList( Class<T> resultClass, Object args )
	{
		return JPASupport.getResultList( this.query, this.entityManager, resultClass, args );
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	public <T> T getSingleResult( Object args )
	{
		return JPASupport.getSingleResult( this.query, this.entityManager, args );
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 *
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	public <T> T getSingleResult( Class<T> resultClass, Object args )
	{
		return JPASupport.getSingleResult( this.query, this.entityManager, resultClass, args );
	}

	/**
	 * Creates a JPA query.
	 *
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	public javax.persistence.Query createQuery( Class<?> resultClass, Object args )
	{
		return JPASupport.createQuery( this.query, this.entityManager, resultClass, args );
	}

	/**
	 * Creates a JPA query.
	 *
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	public javax.persistence.Query createQuery( Object args )
	{
		return JPASupport.createQuery( this.query, this.entityManager, args );
	}

	/**
	 * @return An adapter for Hibernate which enables you to use the query with Hibernate.
	 */
	public HibernateConnectedQueryAdapter hibernate()
	{
		// TODO Better error handling if delegate is not a Hibernate session. But how, can't import hibernate classes here.
		return new HibernateConnectedQueryAdapter( this.query, this.entityManager.getDelegate() );
	}
}
