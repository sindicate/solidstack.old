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

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;


/**
 * 
 * @author René M. de Bloois
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
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 * 
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return A {@link List} of entities.
	 */
	public <T> List< T > getResultList( Map< String, Object > args, EntityManager entityManager, Class< T > entityClass )
	{
		return JPASupport.getResultList( this.query, args, entityManager, entityClass );
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 * 
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return An entity.
	 */
	public <T> T getSingleResult( Map< String, Object > args, EntityManager entityManager, Class< T > entityClass )
	{
		return JPASupport.getSingleResult( this.query, args, entityManager, entityClass );
	}

	/**
	 * Creates a JPA query.
	 * 
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return The JPA query.
	 */
	public javax.persistence.Query createQuery( Map< String, Object > args, EntityManager entityManager, Class< ? > entityClass )
	{
		return JPASupport.createQuery( this.query, args, entityManager, entityClass );
	}
}
