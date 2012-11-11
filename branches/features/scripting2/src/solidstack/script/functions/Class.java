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

package solidstack.script.functions;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.objects.FunctionInstance;

public class Class extends FunctionInstance
{
	static private Map<String, java.lang.Class<?>> cache = new HashMap<String, java.lang.Class<?>>();

	static
	{
		cache.put( "boolean", boolean.class );
		cache.put( "char", char.class );
		cache.put( "byte", byte.class );
		cache.put( "short", short.class );
		cache.put( "int", int.class );
		cache.put( "long", long.class );
		cache.put( "float", float.class );
		cache.put( "double", double.class );
	}

	@Override
	public Object call( List<Object> parameters, ThreadContext thread )
	{
		Assert.isTrue( parameters.size() == 1 );
		Object object = parameters.get( 0 );
		Assert.isTrue( object instanceof String );
		String name = (String)object;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return forName( name, loader );
	}

	static public java.lang.Class<?> forName( String name, ClassLoader loader )
	{
		try
		{
			return java.lang.Class.forName( name, false, loader );
		}
		catch( ClassNotFoundException e )
		{
			java.lang.Class<?> result = cache.get( name );
			if( result != null )
				return result;

			String n = name;
			int dimensions = 0;
			while( n.endsWith( "[]" ) )
			{
				dimensions++;
				n = n.substring( 0, n.length() - 2 );
				result = cache.get( n );
				if( result != null )
				{
					while( dimensions > 0 )
					{
						result = Array.newInstance( result, 0 ).getClass();
						dimensions--;
						n = n + "[]";
						cache.put( n, result );
					}
					return result;
				}
			}

			n = "L" + n + ";";
			while( dimensions > 0 )
			{
				n = "[" + n;
				dimensions--;
			}

			try
			{
				result = java.lang.Class.forName( n, false, loader );
			}
			catch( ClassNotFoundException e1 )
			{
				throw new ScriptException( e );
			}

			return result;
		}
	}
}
