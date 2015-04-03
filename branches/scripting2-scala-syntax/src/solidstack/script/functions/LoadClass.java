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

import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.java.Java;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Type;
import solidstack.script.objects.Util;

public class LoadClass extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 1 )
			throw new ThrowException( "loadClass() expects exactly one parameter", thread.cloneStack() );
		Object object = Util.toJava( parameters[ 0 ] );
		if( object instanceof Class )
			return new Type( (Class)object );
		if( !( object instanceof String ) )
			throw new ThrowException( "loadClass() expects a string or class parameter", thread.cloneStack() );
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try
		{
			return new Type( Java.forName( (String)object, loader ) );
		}
		catch( ClassNotFoundException e )
		{
			throw new ThrowException( "Class not found: " + (String)object, thread.cloneStack() ); // TODO Is this correct exception?
		}
	}
}
