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

import java.lang.reflect.Field;

import solidstack.script.ScriptException;


public class Java
{
	static public Object invoke( Object object, String name, Object... args )
	{
		CallContext context = new CallContext( object, name, args );
		MethodCall call = Resolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}

	static public Object get( Object object, String name )
	{
		try
		{
			Field field = object.getClass().getField( name );
			return field.get( object );
		}
		catch( ReflectiveOperationException e )
		{
			throw new ScriptException( e );
		}
	}

	static public Object construct( Class cls, Object... args )
	{
		CallContext context = new CallContext( cls, null, args );
		MethodCall call = Resolver.resolveConstructorCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}
}
