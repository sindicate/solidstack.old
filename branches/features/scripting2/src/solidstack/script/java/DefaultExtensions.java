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

package solidstack.script.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Default Java extensions.
 */
public class DefaultExtensions
{
	static public Object each( Collection collection, Function function )
	{
		// TODO Or should the ThreadContext be a parameter too?
		Object result = null;
		for( Object object : collection )
			result = function.call( object );
		return result;
	}

	static public List collect( Collection collection, Function function )
	{
		List result = new ArrayList(collection.size());
		for( Object object : collection )
			result.add( function.call( object ) );
		return result;
	}

	static public List collect( Object[] array, Function function )
	{
		List result = new ArrayList(array.length);
		for( Object object : array )
			result.add( function.call( object ) );
		return result;
	}

	static public Object _new( Class cls, Object... args )
	{
		return Java.construct( cls, args );
	}
}
