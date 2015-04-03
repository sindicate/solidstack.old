/*--
 * Copyright 2012 René M. de Bloois
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

package solidstack.hyperdb;

import java.sql.Connection;
import java.util.List;
import java.util.Map;


abstract public class Database
{
	private String name;
	private String url;

	public Database( String name, String url )
	{
		this.name = name;
		this.url = url;
	}

	public String getName()
	{
		return this.name;
	}

	public String getUrl()
	{
		return this.url;
	}

	abstract public Map< String, Schema > getSchemas( Connection connection );
	abstract public List< Table > getTables( Connection connection, String schemaName );
	abstract public List< View > getViews( Connection connection, String schemaName );

	public char getIdentifierQuote()
	{
		return '"';
	}
}
