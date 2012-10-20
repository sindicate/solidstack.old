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

package solidstack.script;

import java.util.HashMap;
import java.util.Map;

public class SubContext extends Context
{
	private Context parent;
	private Map<String, Object> map = new HashMap<String, Object>();

	public SubContext( Context parent )
	{
		this.parent = parent;
	}

	@Override
	public Object get( String name )
	{
		Object result = this.map.get( name );
		if( result == null )
			return this.parent.get( name );
		if( result == Null.INSTANCE )
			return null;
		return result;
	}

	@Override
	public void set( String name, Object value )
	{
		this.map.put( name, value != null ? value : Null.INSTANCE );
	}
}
