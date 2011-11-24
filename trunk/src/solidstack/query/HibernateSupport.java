package solidstack.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;


/**
 * 
 * @author René M. de Bloois
 */
public class HibernateSupport
{
	/**
	 * Retrieves a {@link ResultSet} from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return a {@link ResultSet}.
	 * @see Query#resultSet()
	 */
	static public ResultSet resultSet( final Query query, Session session )
	{
		final ResultHolder< ResultSet > result = new ResultHolder< ResultSet >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.resultSet( connection ) );
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return a {@link List} of {@link Object} arrays.
	 */
	static public List< Object[] > listOfArrays( final Query query, final Session session )
	{
		final ResultHolder< List< Object[] > > result = new ResultHolder< List< Object[] > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.listOfArrays( connection ) );
			}
		});

		return result.get();
	}

	/**
	 * Retrieves a {@link List} of {@link Map}s from the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return A {@link List} of {@link Map}s.
	 */
	static public List< Map< String, Object > > listOfMaps( final Query query, final Session session )
	{
		final ResultHolder< List< Map< String, Object > > > result = new ResultHolder< List< Map< String, Object > > >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.listOfMaps( connection ) );
			}
		});

		return result.get();
	}

	/**
	 * Executes an update (DML) or a DDL query through the given Hibernate {@link Session}.
	 * 
	 * @param query The query.
	 * @param session The Hibernate {@link Session} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws HibernateException SQLExceptions are translated to HibernateExceptions by Hibernate.
	 */
	static public int update( final Query query, Session session )
	{
		final ResultHolder< Integer > result = new ResultHolder< Integer >();

		session.doWork( new Work()
		{
			public void execute( Connection connection )
			{
				result.set( query.update( connection ) );
			}
		});

		return result.get();
	}
}
