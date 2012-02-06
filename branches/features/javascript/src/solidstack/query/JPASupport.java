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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import solidstack.Assert;


/**
 * 
 * @author René M. de Bloois
 */
public class JPASupport
{
	/**
	 * Retrieves a {@link List} of JPA Entities from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return A {@link List} of entities.
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> List< T > getResultList( Query query, Map< String, Object > args, EntityManager entityManager, Class< T > entityClass )
	{
		javax.persistence.Query jpaQuery = createQuery( query, args, entityManager, entityClass );
		return jpaQuery.getResultList();
	}

	/**
	 * Retrieves a single JPA Entity from the given {@link EntityManager}.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return An entity.
	 */
	@SuppressWarnings( "unchecked" )
	static public <T> T getSingleResult( Query query, Map< String, Object > args, EntityManager entityManager, Class< T > entityClass )
	{
		javax.persistence.Query jpaQuery = createQuery( query, args, entityManager, entityClass );
		return (T)jpaQuery.getSingleResult();
	}

	/**
	 * Creates a JPA query.
	 * 
	 * @param query The query.
	 * @param entityManager The {@link EntityManager} to use.
	 * @param entityClass The class to map the results to.
	 * @return The JPA query.
	 */
	static public javax.persistence.Query createQuery( Query query, Map< String, Object > args, EntityManager entityManager, Class< ? > entityClass )
	{
		List< Object > pars = new ArrayList< Object >();
		String preparedSql = query.getPreparedSQL( args, pars );

		javax.persistence.Query result = entityManager.createNativeQuery( preparedSql, entityClass );
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
