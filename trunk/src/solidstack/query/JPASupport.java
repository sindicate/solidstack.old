package solidstack.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	static public <T> List< T > getResultList( Query query, EntityManager entityManager, Class< T > entityClass )
	{
		javax.persistence.Query jpaQuery = createQuery( query, entityManager, entityClass );
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
	static public <T> T getSingleResult( Query query, EntityManager entityManager, Class< T > entityClass )
	{
		javax.persistence.Query jpaQuery = createQuery( query, entityManager, entityClass );
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
	static public javax.persistence.Query createQuery( Query query, EntityManager entityManager, Class< ? > entityClass )
	{
		List< Object > pars = new ArrayList< Object >();
		String preparedSql = query.getPreparedSQL( pars );

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
