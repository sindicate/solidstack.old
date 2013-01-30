/*--
 * Copyright 2011 Ren� M. de Bloois
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


/**
 * Adapts the given Query to JPA.
 *
 * @author Ren� M. de Bloois
 */
public class JPAQueryAdapter
{
	/**
	 * The query that is adapted to JPA.
	 */
	protected Query query;


	/**
	 * Constructor.
	 *
	 * @param query A query to adapt to JPA.
	 */
	public JPAQueryAdapter( Query query )
	{
		this.query = query;
	}

	/**
	 * Executes an update (DML) or a DDL query through the given {@link EntityManager}.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The number of entities updated or deleted.
	 * @see javax.persistence.Query#executeUpdate()
	 */
	public int executeUpdate( EntityManager entityManager, Object args )
	{
		return JPASupport.executeUpdate( this.query, entityManager, args );
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	public <T> List<T> getResultList( EntityManager entityManager, Object args )
	{
		return JPASupport.getResultList( this.query, entityManager, args );
	}

	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of entities.
	 * @see javax.persistence.Query#getResultList()
	 */
	public <T> List<T> getResultList( EntityManager entityManager, Class<T> resultClass, Object args )
	{
		return JPASupport.getResultList( this.query, entityManager, resultClass, args );
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	public <T> T getSingleResult( EntityManager entityManager, Object args )
	{
		return JPASupport.getSingleResult( this.query, entityManager, args );
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return An entity.
	 * @see javax.persistence.Query#getSingleResult()
	 */
	public <T> T getSingleResult( EntityManager entityManager, Class<T> resultClass, Object args )
	{
		return JPASupport.getSingleResult( this.query, entityManager, resultClass, args );
	}

	/**
	 * Creates a JPA query.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param resultClass The class to map the results to.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	public javax.persistence.Query createQuery( EntityManager entityManager, Class<?> resultClass, Object args )
	{
		return JPASupport.createQuery( this.query, entityManager, resultClass, args );
	}

	/**
	 * Creates a JPA query.
	 *
	 * @param entityManager The {@link EntityManager} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The JPA query.
	 * @see EntityManager#createNativeQuery(String, Class)
	 */
	public javax.persistence.Query createQuery( EntityManager entityManager, Object args )
	{
		return JPASupport.createQuery( this.query, entityManager, args );
	}
}
