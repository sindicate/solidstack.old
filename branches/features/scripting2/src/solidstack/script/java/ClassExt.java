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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Extensions to Java classes.
 */
public class ClassExt
{
	static private Map<Class<?>, ClassExt> extensions = new WeakHashMap<Class<?>, ClassExt>();

	static
	{
		for( Method method : DefaultExtensions.class.getMethods() )
			if( ( method.getModifiers() & Modifier.STATIC ) != 0 ) // Only statics
			{
				Class<?>[] types = method.getParameterTypes();
				if( types.length != 0 )
				{
					String name = method.getName().indexOf( '_' ) == 0 ? method.getName().substring( 1 ) : method.getName() ;
					forClass( types[ 0 ] ).addMethod( name, method );
				}
			}
	}

	/**
	 * @param cls A class.
	 * @return The extension for the given class. Null if it doesn't exist.
	 */
	static public ClassExt get( Class<?> cls )
	{
		synchronized( extensions )
		{
			return extensions.get( cls );
		}
	}

	/**
	 * @param cls A class.
	 * @return The extension for the given class. A new one will be created if it doesn't exist.
	 */
	static public ClassExt forClass( Class<?> cls )
	{
		synchronized( extensions )
		{
			ClassExt result = extensions.get( cls );
			if( result != null )
				return result;
			result = new ClassExt();
			extensions.put( cls, result );
			return result;
		}
	}

	// ----------

	private Map<String, ExtMethod> methods = new HashMap<String, ExtMethod>();

	private void addMethod( String name, Method method )
	{
		this.methods.put( name,  new ExtMethod( method ) );
	}

	public ExtMethod getMethod( String name )
	{
		return this.methods.get( name );
	}
}
