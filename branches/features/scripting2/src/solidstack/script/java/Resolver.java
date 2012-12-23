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
import java.util.Iterator;
import java.util.List;

import solidstack.lang.Assert;



public class Resolver
{
	static final public Object[] EMPTY_OBJECT_ARRAY = new Object[ 0 ];


	static public MethodCall resolveMethodCall( CallContext context )
	{
		boolean needStatic = context.getObject() == null;

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

		return Resolver.calculateBestMethodCandidate( context.getCandidates() );
	}

	static public final Class OBJECT_ARRAY = Object[].class;

	static public void collectMethods( Class cls, CallContext context )
	{
		ClassExt ext = ClassExt.forClass( cls );
		if( ext != null )
		{
			// TODO Multiple
			ExtMethod method = ext.getMethod( context.getName() );
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

		if( OBJECT_ARRAY.isAssignableFrom( cls ) && cls != OBJECT_ARRAY )
			collectMethods( Object[].class, context ); // Makes Object[] the virtual super class of all arrays
		else
		{
			cls = cls.getSuperclass();
			if( cls != null )
				collectMethods( cls, context );
		}
	}

	static public MethodCall resolveConstructorCall( CallContext context )
	{
		for( Constructor constructor : context.getType().getConstructors() )
		{
			MethodCall caller = Resolver.matchArguments( context, constructor.getParameterTypes(), ( constructor.getModifiers() & Modifier.TRANSIENT ) != 0 );
			if( caller != null )
			{
				caller.constructor = constructor;
				context.addCandidate( caller );
			}
		}

		return Resolver.calculateBestMethodCandidate( context.getCandidates() );
	}







    // ---------- STEP 1: Used to match argument values with argument types

	static public MethodCall matchArguments( CallContext context, Class[] types, boolean vararg )
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










	// ---- STEP 2: Used to calculate the best candidate

	// TODO (RMB) Can we exchange precedence for the specificity of 'this'
    static public MethodCall calculateBestMethodCandidate( List<MethodCall> candidates ) throws ResolverException
    {
		if( candidates.size() == 0 )
			return null;
		if( candidates.size() == 1 )
			return candidates.get( 0 );

		List< MethodCall > best = candidates;

		// --------------------
//		NestingLogger.message( "Step 3, determining no-vararg candidates, from " + best.size() + " candidates" );

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

//		// --------------------
////		NestingLogger.message( "Step 4, determining most specific candidate, from " + best.size() + " candidates" );
//
		best2 = new ArrayList( best );
		int index = 0;
		while( index < best2.size() - 1 )
		{
			MethodCall candidate1 = best2.get( index );
			MethodCall candidate2 = best2.get( index + 1 );
			// TODO Not isVarargCall, but isVarargMethod
			if( moreSpecificThan( candidate1.getParameterTypes(), candidate1.isVarargCall, candidate2.getParameterTypes(), candidate2.isVarargCall ) )
			{
				if( moreSpecificThan( candidate2.getParameterTypes(), candidate2.isVarargCall, candidate1.getParameterTypes(), candidate1.isVarargCall ) )
					index ++;
				else
					best2.remove( index + 1 );
			}
			else
			{
				if( moreSpecificThan( candidate2.getParameterTypes(), candidate2.isVarargCall, candidate1.getParameterTypes(), candidate1.isVarargCall ) )
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

//		// --------------------
////		NestingLogger.message( "Step 5, calculating distances of " + best.size() + " candidates" );
//
//		// TODO (RMB) Also check for 2 or more smallest distances
//		best2 = new ArrayList< MethodCall >();
//
//		int candidateDistance = 0;
////		int precedence = 0;
//		for( MethodCall method : best )
//		{
//			int distance = calculateArgumentsDistance( method.getParameterTypes(), method.getArgs() );
//			if( distance >= 0 )
//				if( best2.size() == 0 || distance < candidateDistance /* || distance == candidateDistance && method.precedence < precedence */ )
//				{
//					best2.clear();
//					best2.add( method );
//					candidateDistance = distance;
////					precedence = method.precedence;
//				}
//				else if( distance == candidateDistance /* && method.precedence == precedence */ )
//				{
//					best2.add( method );
//				}
//		}
//
//		if( best2.size() == 1 )
//			return best2.get( 0 );
//
//		if( best2.size() > 1 )
//			best = best2;
//
//		if( best.size() > 1 )
			throw new ResolverException( best );

//		NestingLogger.message( "Step 4, comparing varargs for " + best.size() + " candidates");
//
//		List< MethodCall > best3 = new ArrayList< MethodCall >();
//
//		int minIndex = 0;
//		for( MethodCall method : best2 )
//		{
//			int varArgIndex = method.getVarArgIndex();
//			NestingLogger.message( "    vararg index: " + index );
//			if( best3.size() == 0 || varArgIndex == minIndex )
//			{
//				best3.add( method );
//				minIndex = varArgIndex;
//			}
//			else if( varArgIndex < minIndex )
//			{
//				best3.clear();
//				best3.add( method );
//				minIndex = varArgIndex;
//			}
//		}
//
//		if( best3.size() == 1 )
//			return best3.get( 0 );
//		if( best3.size() == 0 )
//			return null;



		// --------------------
//		throw new ScriptException( "impossible" );
    }


//	public static int calculateArgumentsDistance( Class[] types, Object[] args )
//	{
//		// TODO (RMB) We don't have the old vararg penalty yet (x<<28)
//
//    	// Varargs are already applied by matchArguments
//
//		int ret = 0;
//		for( int i = /* includesThis ? 1 : */ 0; i < types.length; i++ )
//		{
//			// TODO (RMB) vararg distance?
//			Object arg = args[ i ];
//			int distance = Types.calculateDistance( arg != null ? arg.getClass() : null, types[ i ] );
//			if( distance < 0 )
//				return -1;
//			ret += distance;
//		}
//
//		return ret;
//	}


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
				if( !Types.assignable( types[ i ], otherType ) )
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
				if( !Types.assignable( type, otherTypes[ i ] ) )
					return false;
			if( otherVararg )
			{
				Class otherType = otherTypes[ lastType ].getComponentType();
				if( !Types.assignable( type, otherType ) )
					return false;
			}
		}
		else if( vararg && otherVararg )
		{
			Class type = types[ lastArg ].getComponentType();
			Class otherType = otherTypes[ lastType ].getComponentType();
			if( !Types.assignable( type, otherType ) )
				return false;
		}

		return true;
	}










	// ---- STEP 3: Used to call the method

	static public Object[] transformArguments( Class[] types, Object[] args )
	{
		Object[] result = new Object[ args.length ];
		for( int i = types.length - 1; i >= 0; i-- )
			result[ i ] = Types.convert( args[ i ], types[ i ] );
		return result;
	}
}
