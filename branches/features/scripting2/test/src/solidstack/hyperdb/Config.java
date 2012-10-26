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

import java.util.LinkedHashMap;
import java.util.Map;

public class Config
{
	static private Map< String, Database > databases;

	static
	{
		databases = new LinkedHashMap<String, Database>();
//		databases.put( "TAXI", new Database( "TAXI", "jdbc:oracle:thin:@192.168.0.109:1521:XE" ) );
	}

	static public Map< String, Database > getDatabases()
	{
		return databases;
	}

	public static Database getDatabase( String database )
	{
		return databases.get( database );
	}
}
