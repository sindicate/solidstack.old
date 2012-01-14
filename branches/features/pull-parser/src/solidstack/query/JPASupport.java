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
	public List<?> listOfEntities(Query query, EntityManager entityManager, Class<?> entityClass) {

		List< Object > pars = new ArrayList< Object >();
		String preparedSql = query.getPreparedSQL( pars );

		javax.persistence.Query jpaQuery = entityManager.createNativeQuery( preparedSql, entityClass );
		int i = 0;
		for( Object par : pars )
		{
			if( par != null )
			{
				Assert.isFalse( par instanceof Collection );
				Assert.isFalse( par.getClass().isArray() );
			}
			jpaQuery.setParameter( ++i, par );
		}

		return jpaQuery.getResultList();
	}
}
