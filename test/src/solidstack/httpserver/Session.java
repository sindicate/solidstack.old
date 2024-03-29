/*--
 * Copyright 2012 Ren� M. de Bloois
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

package solidstack.httpserver;

import java.util.HashMap;
import java.util.Map;

public class Session
{
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public void setAttribute( String name, Object value )
	{
//		Loggers.httpServer.debug( "setAttribute, session: {}, {}", DebugId.getId( this ), name );
		this.attributes.put( name, value );
	}

	public Object getAttribute( String name )
	{
//		Loggers.httpServer.debug( "getAttribute, session: {}, {}", DebugId.getId( this ), name );
		return this.attributes.get( name );
	}
}
