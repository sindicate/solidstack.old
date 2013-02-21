package solidstack.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import solidstack.lang.Assert;


/**
 * Prepared SQL combined with a parameter list.
 *
 * @author René de Bloois
 */
public class PreparedQuery
{
	private String sql;
	private List< Object > parameters;
	private ResultModel resultModel;

	/**
	 * Constructor.
	 *
	 * @param sql The prepared SQL string.
	 * @param parameters The parameter list.
	 */
	protected PreparedQuery( String sql, List< Object > parameters, ResultModel resultModel )
	{
		this.sql = sql;
		this.parameters = parameters;
		this.resultModel = resultModel;
	}

	/**
	 * Returns the prepared SQL string.
	 *
	 * @return The prepared SQL string.
	 */
	public String getSQL()
	{
		return this.sql;
	}

	/**
	 * Returns the parameter list.
	 *
	 * @return The parameter list.
	 */
	public List< Object > getParameters()
	{
		return this.parameters;
	}

	public ResultModel getResultModel()
	{
		return this.resultModel;
	}

	public PreparedStatement prepareStatement( Connection connection )
	{
		try
		{
			PreparedStatement statement = connection.prepareStatement( this.sql );
			int i = 0;
			for( Object par : this.parameters )
			{
				if( par == null )
				{
					// Tested in Oracle with an INSERT
					statement.setNull( ++i, Types.NULL );
				}
				else
				{
					Assert.isFalse( par instanceof Collection );
					Assert.isFalse( par.getClass().isArray() );
					statement.setObject( ++i, par );
				}
			}
			return statement;
		}
		catch( SQLException e )
		{
			throw new QuerySQLException( e );
		}
	}
}
