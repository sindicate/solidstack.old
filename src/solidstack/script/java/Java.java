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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


/**
 * Contains methods to call methods an objects, get or set fields on objects.
 */
public class Java
{
	/**
	 * Invokes a method on an object. Exceptions are passed through as-is, even checked exceptions.
	 *
	 * @param object An object instance.
	 * @param name The method name.
	 * @param args The arguments to the method.
	 * @return The result of calling the method.
	 * @throws InvocationTargetException Wraps the exception thrown by the underlying method.
	 * @throws MissingMethodException
	 */
	static public Object invoke( Object object, String name, Object... args ) throws InvocationTargetException, MissingMethodException
	{
		CallContext context = new CallContext( object, name, args );
		MethodCall call = Resolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}

	/**
	 * Invokes a static method on a class. Exceptions are passed through as-is, even checked exceptions.
	 *
	 * @param type A class.
	 * @param name The method name.
	 * @param args The arguments to the method.
	 * @return The result of calling the method.
	 * @throws InvocationTargetException Wraps the exception thrown by the underlying method.
	 * @throws MissingMethodException
	 */
	static public Object invokeStatic( Class<?> type, String name, Object... args ) throws InvocationTargetException, MissingMethodException
	{
		CallContext context = new CallContext( type, name, args );
		MethodCall call = Resolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}

	/**
	 * Reads a field of an object.
	 *
	 * @param object An object.
	 * @param name The name of the field.
	 * @return The value of the field.
	 * @throws MissingFieldException
	 */
	static public Object get( Object object, String name ) throws MissingFieldException
	{
		try
		{
			return object.getClass().getField( name ).get( object );
		}
		catch( NoSuchFieldException e )
		{
			throw new MissingFieldException( object, object.getClass(), name );
		}
		catch( IllegalAccessException e )
		{
			throw throwUnchecked( e );
		}
	}

	/**
	 * Reads a static field of a class.
	 *
	 * @param type A class.
	 * @param name The name of the field.
	 * @return The value of the field.
	 * @throws MissingFieldException
	 */
	public static Object getStatic( Class<?> type, String name ) throws MissingFieldException
	{
		try
		{
			Field field = type.getField( name );
			if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
				throw new MissingFieldException( null, type, name );
			return type.getField( name ).get( null );
		}
		catch( NoSuchFieldException e )
		{
			throw new MissingFieldException( null, type, name );
		}
		catch( IllegalAccessException e )
		{
			throw throwUnchecked( e );
		}
	}

	/**
	 * Instantiates an object.
	 *
	 * @param type The class of the object to be instantiated.
	 * @param args The arguments to the constructor.
	 * @return The instantiated object.
	 * @throws InvocationTargetException Wraps the exception thrown by the underlying method.
	 * @throws MissingMethodException
	 */
	static public Object construct( Class<?> type, Object... args ) throws InvocationTargetException, MissingMethodException
	{
		CallContext context = new CallContext( type, null, args );
		MethodCall call = Resolver.resolveConstructorCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}

	/**
	 * Throws the given throwable without the need to declare it.
	 *
	 * @param throwable The throwable.
	 * @return RuntimeException so that you could add 'throw' and don't need to add 'return' after calling this method.
	 */
	static public RuntimeException throwUnchecked( Throwable throwable )
	{
		throw Java.<RuntimeException>unchecked( throwable );
	}

	@SuppressWarnings( "unchecked" )
	static private <T extends Throwable> T unchecked( Throwable exception )
	{
		// No 'checkcast' byte code is added, because the parameter is a Throwable and the lower bound of T is also Throwable.
		return (T)exception;
	}

	static private Map<String, java.lang.Class<?>> primitiveCache = new HashMap<String, java.lang.Class<?>>();

	static
	{
		primitiveCache.put( "boolean", boolean.class );
		primitiveCache.put( "char", char.class );
		primitiveCache.put( "byte", byte.class );
		primitiveCache.put( "short", short.class );
		primitiveCache.put( "int", int.class );
		primitiveCache.put( "long", long.class );
		primitiveCache.put( "float", float.class );
		primitiveCache.put( "double", double.class );
		primitiveCache.put( "void", void.class );
	}

	static public java.lang.Class<?> forName( String name, ClassLoader loader ) throws ClassNotFoundException
	{
		try
		{
			return java.lang.Class.forName( name, false, loader );
		}
		catch( ClassNotFoundException e )
		{
			java.lang.Class<?> result = primitiveCache.get( name );
			if( result != null )
				return result;

			String n = name;
			int dimensions = 0;
			while( n.endsWith( "[]" ) )
			{
				dimensions++;
				n = n.substring( 0, n.length() - 2 );
				result = primitiveCache.get( n );
				if( result != null )
				{
					while( dimensions > 0 )
					{
						result = Array.newInstance( result, 0 ).getClass();
						dimensions--;
						n = n + "[]";
						primitiveCache.put( n, result );
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
			catch( ClassNotFoundException ee )
			{
				// TODO We can also do . -> $ replacement to search for inner classes
				throw e; // Throw the original
			}

			return result;
		}
	}
}
