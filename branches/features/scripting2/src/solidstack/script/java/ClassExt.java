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
import java.util.IdentityHashMap;
import java.util.Map;

import solidstack.lang.Assert;


public class ClassExt
{
	static private IdentityHashMap<Class, ClassExt> extensions = new IdentityHashMap<Class, ClassExt>();

	static
	{
		for( Method method : DefaultExtensions.class.getMethods() )
			if( ( method.getModifiers() & Modifier.STATIC ) != 0 )
			{
				Class[] types = method.getParameterTypes();
				Assert.isFalse( types.length == 0 );
				Class cls = types[ 0 ];
				ClassExt ext = forClass( cls, true );
				ext.addMethod( method );
			}
	}

	static public ClassExt forClass( Class cls )
	{
		return extensions.get( cls );
	}

	static public ClassExt forClass( Class cls, boolean create )
	{
		ClassExt result = forClass( cls );
		if( result != null )
			return result;
		result = new ClassExt();
		extensions.put( cls, result );
		return result;
	}

	private Map<String, Method> methods = new HashMap<String, Method>();

	private void addMethod( Method method )
	{
		this.methods.put( method.getName(), method );
	}

	public Method getMethod( String name )
	{
		return this.methods.get( name );
	}
}
