package solidstack.query;

import java.util.List;

/**
 * Prepared SQL combined with a parameter list.
 *
 * @author René de Bloois
 */
public class PreparedQuery
{
	private String sql;
	private List< Object > pars;
	private ResultModel resultModel;

	/**
	 * Constructor.
	 *
	 * @param sql The prepared SQL string.
	 * @param pars The parameter list.
	 */
	protected PreparedQuery( String sql, List< Object > pars, ResultModel resultModel )
	{
		this.sql = sql;
		this.pars = pars;
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
		return this.pars;
	}

	public ResultModel getResultModel()
	{
		return this.resultModel;
	}
}
