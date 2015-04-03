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
import java.util.Map;
import java.util.Map.Entry;


/**
 * Default Java extensions.
 */
public class DefaultClassExtensions
{
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

	static public Object each( Collection collection, Function function )
	{
		// TODO Or should the ThreadContext be a parameter too?
		int count = function.getParameters().length;
		if( count == 2 )
		{
			Object result = null;
			int index = 0;
			for( Object object : collection )
				result = function.call( index++, object );
			return result;
		}
		Object result = null;
		for( Object object : collection )
			result = function.call( object );
		return result;
	}

	static public Object each( Map<?,?> map, Function function )
	{
		Object result = null;
		for( Entry<?,?> entry : map.entrySet() )
			result = function.call( entry.getKey(), entry.getValue() );
		return result;
	}

	static public Object eachKey( Map<?,?> map, Function function )
	{
		Object result = null;
		for( Object key : map.keySet() )
			result = function.call( key );
		return result;
	}

	static public int size( Object[] array )
	{
		return array.length;
	}

	static public int size( boolean[] array )
	{
		return array.length;
	}

	static public int size( char[] array )
	{
		return array.length;
	}

	static public int size( byte[] array )
	{
		return array.length;
	}

	static public int size( short[] array )
	{
		return array.length;
	}

	static public int size( int[] array )
	{
		return array.length;
	}

	static public int size( long[] array )
	{
		return array.length;
	}

	static public int size( float[] array )
	{
		return array.length;
	}

	static public int size( double[] array )
	{
		return array.length;
	}

	static public int size( String string )
	{
		return string.length();
	}
}
