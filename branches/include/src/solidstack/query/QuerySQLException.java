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

import java.sql.SQLException;

/**
 * This exception wraps a {@link SQLException}.
 * 
 * @author René M. de Bloois
 * @since March 8, 2011
 */
public class QuerySQLException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new query exception from the given {@link SQLException}.
	 * 
	 * @param cause The cause.
	 */
	public QuerySQLException( SQLException cause )
	{
		super( cause );
		if( cause == null )
			throw new NullPointerException( "cause cannot be null" );
	}

	/**
	 * Returns the {@link SQLException} that caused this exception.
	 * 
	 * @return The SQLException.
	 */
	public SQLException getSQLException()
	{
		return (SQLException)getCause();
	}
}
