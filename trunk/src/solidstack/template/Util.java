/*--
 * Copyright 2006 René M. de Bloois
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

package solidstack.template;

import solidstack.SystemException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

/**
 * Generic utilities.
 * 
 * @author René M. de Bloois
 */
public class Util
{
	/**
	 * Returns a new instance of the given class.
	 * 
	 * @param <T> The type of the class.
	 * @param cls The class.
	 * @return A new instance of the given class.
	 */
	static public <T> T newInstance( Class< T > cls )
	{
		try
		{
			return cls.newInstance();
		}
		catch( InstantiationException e )
		{
			throw new SystemException( e );
		}
		catch( IllegalAccessException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Parses a class with the given {@link GroovyClassLoader}.
	 * 
	 * @param loader The {@link GroovyClassLoader} to use.
	 * @param source The source of the class.
	 * @return The class.
	 */
	@SuppressWarnings( "unchecked" )
	static public Class< GroovyObject > parseClass( GroovyClassLoader loader, GroovyCodeSource source )
	{
		return loader.parseClass( source );
	}
}
