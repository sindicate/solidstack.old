package solidstack.query.eclipselink;

import java.sql.Connection;
import java.sql.ResultSet;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.Session;

import solidstack.query.Query;

public class EclipseLinkSupport
{
	static public ResultSet resultSet( final Query query, final Session session, final Object args )
	{
		return (ResultSet)session.executeQuery( new DatabaseQuery()
		{
			@Override
			public Object executeDatabaseQuery() throws DatabaseException, OptimisticLockException
			{
				Connection connection = this.session.getAccessor().getConnection();
				return query.resultSet( connection, args );
				// TODO What about the exceptions?
			}
		} );
	}
}
