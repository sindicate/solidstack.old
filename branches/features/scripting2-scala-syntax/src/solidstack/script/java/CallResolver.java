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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;



public class CallResolver
{
	static public final Object[] EMPTY_OBJECT_ARRAY = new Object[ 0 ];
	static public final Class OBJECT_ARRAY_CLASS = Object[].class;

	static private final Map<CallSignature, MethodHandle> cache = new HashMap<CallSignature, MethodHandle>();


	static public MethodCall resolveMethodCall( CallResolutionContext context )
	{
		MethodHandle handle = cache.get( context.getCallKey() );
		if( handle != null )
		{
//			System.out.println( context.getName() + " hit" );
			MethodCall caller = new MethodCall( handle.isVarargCall );
			caller.constructor = handle.constructor;
			caller.method = handle.method;
			caller.extMethod = handle.extMethod;
			caller.object = context.getObject();
			caller.args = context.getArgs();
			return caller;
		}

//		System.out.println( context.getName() + " misss" );

		MethodCall result = resolveMethodCall0( context );

		if( result != null )
			cache.put( context.getCallKey(), new MethodHandle( result.method, result.extMethod, result.constructor, result.isVarargCall ) );

		return result;
	}


	static private MethodCall resolveMethodCall0( CallResolutionContext context )
	{
		boolean needStatic = context.staticCall();

		for( Method method : context.getType().getMethods() )
			if( !needStatic || ( method.getModifiers() & Modifier.STATIC ) != 0 )
				if( method.getName().equals( context.getName() ) )
				{
					MethodCall caller = matchArguments( context, method.getParameterTypes(), ( method.getModifiers() & Modifier.TRANSIENT ) != 0 );
					if( caller != null )
					{
						caller.object = context.getObject();
						caller.method = method;
						context.addCandidate( caller );
					}
				}

		if( !needStatic )
			collectMethods( context.getType(), context );
		else
			collectStaticMethods( context.getType(), context );

		return CallResolver.calculateBestMethodCandidate( context.getCandidates() );
	}


	static public void collectMethods( Class cls, CallResolutionContext context )
	{
		ClassExtension ext = ClassExtension.forClass( cls );
		if( ext != null )
		{
			// TODO Multiple
			ExtensionMethod method = ext.getMethod( context.getName() );
			if( method != null )
			{
				MethodCall caller = matchArguments( context, method.getParameterTypes(), method.isVararg() );
				if( caller != null )
				{
					caller.object = context.getObject();
					caller.extMethod = method;
					context.addCandidate( caller );
				}
			}
		}

		Class[] interfaces = cls.getInterfaces();
		for( Class iface : interfaces )
		{
			if( !context.isInterfaceDone( iface ) )
			{
				collectMethods( iface, context );
				context.interfaceDone( iface );
			}
		}

		if( OBJECT_ARRAY_CLASS.isAssignableFrom( cls ) && cls != OBJECT_ARRAY_CLASS )
			collectMethods( Object[].class, context ); // Makes Object[] the virtual super class of all arrays
		else
		{
			cls = cls.getSuperclass();
			if( cls != null )
				collectMethods( cls, context );
		}
	}

	static public void collectStaticMethods( Class cls, CallResolutionContext context )
	{
		ClassExtension ext = ClassExtension.forClass( cls );
		if( ext != null )
		{
			// TODO Multiple
			ExtensionMethod method = ext.getStaticMethod( context.getName() );
			if( method != null )
			{
				MethodCall caller = CallResolver.matchArguments( context, method.getParameterTypes(), method.isVararg() );
				if( caller != null )
				{
					caller.object = context.getObject();
					caller.extMethod = method;
					context.addCandidate( caller );
				}
			}
		}
	}

	static public MethodCall resolveConstructorCall( CallResolutionContext context )
	{
		for( Constructor constructor : context.getType().getConstructors() )
		{
			MethodCall caller = CallResolver.matchArguments( context, constructor.getParameterTypes(), ( constructor.getModifiers() & Modifier.TRANSIENT ) != 0 );
			if( caller != null )
			{
				caller.constructor = constructor;
				context.addCandidate( caller );
			}
		}

		return CallResolver.calculateBestMethodCandidate( context.getCandidates() );
	}


	static public MethodCall matchArguments( CallResolutionContext context, Class[] types, boolean vararg )
	{
		// Initialize all used values from the context
		Class[] argTypes = context.getArgTypes();

		int argCount = argTypes.length;
		int typeCount = types.length;
		int lastType = typeCount - 1;

		if( vararg )
		{
			if( argCount < lastType )
				return null;
			// If the last argument's type is equal to the type of the last parameter, then it is NOT vararg.
			if( argCount == typeCount )
				if( argTypes[ argCount - 1 ] == types[ lastType ] )
					vararg = false;
		}
		else
		{
			if( argCount != typeCount )
				return null;
		}

		if( !vararg )
		{
			for( int i = 0; i < argCount; i++ )
				if( !Types.assignable( argTypes[ i ], types[ i ] ) )
					return null;
			return new MethodCall( false );
			}

		// Varargs

		for( int i = 0; i < lastType; i++ )
			if( !Types.assignable( argTypes[ i ], types[ i ] ) )
				return null;

		int diff = argCount - lastType;
		Class componentType = types[ lastType ].getComponentType();
		for( int i = 0; i < diff; i++ )
			if( !Types.assignable( argTypes[ i + lastType ], componentType ) )
				return null;

		return new MethodCall( true );
	}


    static public MethodCall calculateBestMethodCandidate( List<MethodCall> candidates ) throws CallResolutionException
    {
		if( candidates.size() == 0 )
			return null;
		if( candidates.size() == 1 )
			return candidates.get( 0 );

		List< MethodCall > best = candidates;

		// Filter out no-vararg candidates

		ArrayList<MethodCall> best2 = new ArrayList( best );
		for( Iterator iterator = best2.iterator(); iterator.hasNext(); )
		{
			MethodCall candidate = (MethodCall)iterator.next();
			if( candidate.isVarargCall )
				iterator.remove();
		}

		if( best2.size() == 1 )
			return best2.get( 0 );

		if( best2.size() > 0 )
			best = best2;

//		Determine most specific candidate

		best2 = new ArrayList( best );
		int index = 0;
		while( index < best2.size() - 1 )
		{
			MethodCall candidate1 = best2.get( index );
			MethodCall candidate2 = best2.get( index + 1 );
			if( moreSpecificThan( candidate1.getParameterTypes(), candidate1.isVararg(), candidate2.getParameterTypes(), candidate2.isVararg() ) )
			{
				if( moreSpecificThan( candidate2.getParameterTypes(), candidate2.isVararg(), candidate1.getParameterTypes(), candidate1.isVararg() ) )
					index ++;
				else
					best2.remove( index + 1 );
		}
			else
			{
				if( moreSpecificThan( candidate2.getParameterTypes(), candidate2.isVararg(), candidate1.getParameterTypes(), candidate1.isVararg() ) )
				{
					best2.remove( index );
					if( index > 0 )
						index--;
				}
				else
					index ++;
			}
		}

		if( best2.size() == 1 )
			return best2.get( 0 );

		if( best2.size() > 0 )
			best = best2;

		throw new CallResolutionException( best );
    }


	// TODO Actually, vararg en nonvararg are currently never compared to each other
	static public boolean moreSpecificThan( Class[] types, boolean vararg, Class[] otherTypes, boolean otherVararg )
	{
		int argCount = types.length;
		int typeCount = otherTypes.length;

		if( !vararg && !otherVararg )
			Assert.isTrue( argCount == typeCount );

		int lastArg = argCount; if( vararg ) lastArg --;
		int lastType = typeCount; if( otherVararg ) lastType --;

		int i = 0;
		while( i < lastArg && i < lastType )
			if( !Types.assignable( types[ i ], otherTypes[ i++ ] ) )
				return false;

		if( i < lastArg )
		{
			Class otherType = otherTypes[ lastType ].getComponentType();
			while( i < lastArg )
				if( !Types.assignable( types[ i++ ], otherType ) )
					return false;
			if( vararg )
		{
				Class type = types[ lastArg ].getComponentType();
				if( !Types.assignable( type, otherType ) )
					return false;
		}
			}
		else if( i < lastType )
		{
			Class type = types[ lastArg ].getComponentType();
			while( i < lastType )
				if( !Types.assignable( type, otherTypes[ i++ ] ) )
					return false;
			if( otherVararg )
			{
				Class otherType = otherTypes[ lastType ].getComponentType();
				if( !Types.assignable( type, otherType ) )
					return false;
			}
		}
		else if( vararg )
		{
			if( otherVararg )
			{
				Class type = types[ lastArg ].getComponentType();
				Class otherType = otherTypes[ lastType ].getComponentType();
				if( !Types.assignable( type, otherType ) )
					return false;
				}
			else
				return false;
				}

		return true;
				}
				}
