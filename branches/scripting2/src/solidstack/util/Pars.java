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

package solidstack.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Parameters support class that makes it easier to build up a map of parameters.
 *
 * @author René de Bloois
 */
public class Pars extends HashMap< String, Object >
{
	private static final long serialVersionUID = 1L;

	/**
	 * An empty map.
	 */
	static public final Map< String, Object > EMPTY = Collections.emptyMap();


	/**
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 */
	public Pars( String name, Object value )
	{
		set( name, value );
	}

	/**
	 * @param nameValue Pairs of names and values.
	 */
	public Pars( Object... nameValue )
	{
		set( nameValue );
	}

	public Pars( Map< String, Object > pars )
	{
		putAll( pars );
	}

	/**
	 * Set the given parameter.
	 *
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @return This object so that you can chain set calls.
	 */
	public Pars set( String name, Object value )
	{
		put( name, value );
		return this;
	}

	/**
	 * Set the given parameters.
	 *
	 * @param nameValue Pairs of names and values.
	 * @return This object so that you can chain set calls.
	 */
	public Pars set( Object... nameValue )
	{
		int len = nameValue.length;
		if( len % 2 != 0 )
			throw new IllegalArgumentException( "Need an even arg count (name/value pairs)" );
		for( int i = 0; i < len; )
		{
			if( !( nameValue[ i ] instanceof String ) )
				throw new IllegalArgumentException( "Need name/value pairs, name must be String" );
			put( (String)nameValue[ i++ ], nameValue[ i++ ] );
		}
		return this;
	}
}
