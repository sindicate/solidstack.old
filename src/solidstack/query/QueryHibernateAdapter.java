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
public class QueryHibernateAdapter
{
	protected Query query;

	public QueryHibernateAdapter( Query query )
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
