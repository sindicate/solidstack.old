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

package solidstack.query.eclipselink;

import java.sql.Connection;
import java.sql.ResultSet;

import org.eclipse.persistence.sessions.Session;

import solidstack.query.Query;


/**
 * Adapts the given Query to EclipseLink.
 *
 * @author René M. de Bloois
 */
public class EclipseLinkQueryAdapter
{
	/**
	 * The query that is adapted to EclipseLink.
	 */
	protected Query query;


	/**
	 * Constructor.
	 *
	 * @param query A query to adapt to EclipseLink.
	 */
	public EclipseLinkQueryAdapter( Query query )
	{
		this.query = query;
	}

	/**
	 * Retrieves a {@link ResultSet} from the given EclipeLink {@link Session}.
	 *
	 * @param session The EclipseLink {@link Session} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return a {@link ResultSet}.
	 * @see Query#resultSet(Connection, Object)
	 */
	public ResultSet resultSet( Session session, Object args )
	{
		return EclipseLinkSupport.resultSet( this.query, session, args );
	}
}
