/*--
 * Copyright 2012 Ren� M. de Bloois
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
import java.lang.reflect.InvocationTargetException;
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
		CallResolutionContext context = CallResolutionContext.forMethodCall( object, name, args );
		MethodCall call = CallResolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		addArgs( call, args );
		return call.invoke();
	}

	static private void addArgs( MethodCall call, Object... args )
	{
		if( !call.isVarargCall )
		{
			call.setArgs( args );
			return;
		}

		int count = call.getParameterTypes().length;
		if( count == 1 )
		{
			call.setArgs( new Object[] { args } );
			return;
		}

		Object[] pars = new Object[ count ];
		count--;
		System.arraycopy( args, 0, pars, 0, count );
		Object[] varargs = new Object[ args.length - count ];
		System.arraycopy( args, count, varargs, 0, varargs.length );
		call.setArgs( varargs );
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
		CallResolutionContext context = CallResolutionContext.forMethodCall( type, name, args );
		MethodCall call = CallResolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		addArgs( call, args );
		return call.invoke();
	}

	/**
	 * Reads a field of an object.
	 *
	 * @param object An object.
	 * @param name The name of the field.
	 * @return The value of the field.
	 * @throws MissingFieldException
	 * @throws InvocationTargetException
	 */
	static public Object get( Object object, String name ) throws MissingFieldException, InvocationTargetException
	{
		CallResolutionContext context = CallResolutionContext.forPropertyRead( object, name );
		MethodCall call = CallResolver.resolvePropertyRead( context );
		if( call == null )
			throw new MissingFieldException( object, object.getClass(), name ); // TODO MissingPropertyException?
		return call.invoke();
	}

	static public Object set( Object object, String name, Object value ) throws MissingFieldException, InvocationTargetException
	{
		CallResolutionContext context = CallResolutionContext.forPropertyWrite( object, name, value );
		MethodCall call = CallResolver.resolvePropertyWrite( context );
		if( call == null )
			throw new MissingFieldException( object, object.getClass(), name ); // TODO MissingPropertyException?
		addArgs( call, value );
		return call.invoke();
	}

	/**
	 * Reads a static field of a class.
	 *
	 * @param type A class.
	 * @param name The name of the field.
	 * @return The value of the field.
	 * @throws MissingFieldException
	 * @throws InvocationTargetException
	 */
	public static Object getStatic( Class<?> type, String name ) throws MissingFieldException, InvocationTargetException
	{
		CallResolutionContext context = CallResolutionContext.forPropertyRead( type, name );
		MethodCall call = CallResolver.resolvePropertyRead( context );
		if( call == null )
			throw new MissingFieldException( null, type, name ); // TODO MissingPropertyException?
		return call.invoke();
	}

	/**
	 * Writes static field of a class.
	 *
	 * @param type A class.
	 * @param name The name of the field.
	 * @param value The value to write.
	 * @throws MissingFieldException
	 * @throws InvocationTargetException
	 */
	public static Object setStatic( Class<?> type, String name, Object value ) throws MissingFieldException, InvocationTargetException
	{
		CallResolutionContext context = CallResolutionContext.forPropertyWrite( type, name, value );
		MethodCall call = CallResolver.resolvePropertyWrite( context );
		if( call == null )
			throw new MissingFieldException( null, type, name ); // TODO MissingPropertyException?
		addArgs( call, value );
		return call.invoke();
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
		CallResolutionContext context = CallResolutionContext.forMethodCall( type, null, args );
		MethodCall call = CallResolver.resolveConstructorCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		addArgs( call, args );
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
