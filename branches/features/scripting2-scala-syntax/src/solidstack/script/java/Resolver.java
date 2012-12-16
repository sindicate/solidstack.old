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

import groovy.lang.GroovyRuntimeException;

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
					MethodCall caller = Resolver.matchArguments( context, method.getParameterTypes(), ( method.getModifiers() & Modifier.TRANSIENT ) != 0, false );
					if( caller != null )
					{
						caller.object = context.getObject();
						caller.method = method;
						context.addCandidate( caller );
					}
				}

		context.setThisMode( true );
		if( !needStatic )
			collectMethods( context.getType(), context );
		else
			collectStaticMethods( context.getType(), context );
		context.setThisMode( false );

		return Resolver.calculateBestMethodCandidate( context.getCandidates() );
	}

	static public void collectMethods( Class cls, CallContext context )
	{
		ClassExt ext = ClassExt.forClass( cls );
		if( ext != null )
		{
			// TODO Multiple
			Method method = ext.getMethod( context.getName() );
			if( method != null )
			{
				MethodCall caller = Resolver.matchArguments( context, method.getParameterTypes(), ( method.getModifiers() & Modifier.TRANSIENT ) != 0, true );
				if( caller != null )
				{
					caller.object = context.getObject();
					caller.method = method;
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

		if( cls.isArray() && cls != Object[].class )
			collectMethods( Object[].class, context ); // Makes Object[] the virtual super class of all arrays
		else
		{
			cls = cls.getSuperclass();
			if( cls != null )
				collectMethods( cls, context );
		}
	}

	static public void collectStaticMethods( Class cls, CallContext context )
	{
		ClassExt ext = ClassExt.forClass( cls );
		if( ext != null )
		{
			// TODO Multiple
			Method method = ext.getStaticMethod( context.getName() );
			if( method != null )
			{
				MethodCall caller = Resolver.matchArguments( context, method.getParameterTypes(), ( method.getModifiers() & Modifier.TRANSIENT ) != 0, true );
				if( caller != null )
				{
					caller.object = context.getObject();
					caller.method = method;
					context.addCandidate( caller );
				}
			}
		}
	}

	static public MethodCall resolveConstructorCall( CallContext context )
	{
		for( Constructor constructor : context.getType().getConstructors() )
		{
			MethodCall caller = Resolver.matchArguments( context, constructor.getParameterTypes(), ( constructor.getModifiers() & Modifier.TRANSIENT ) != 0, false );
			if( caller != null )
			{
				caller.constructor = constructor;
				context.addCandidate( caller );
			}
		}

		return Resolver.calculateBestMethodCandidate( context.getCandidates() );
	}







    // ---------- STEP 1: Used to match argument values with argument types

	static public MethodCall matchArguments( CallContext context, Class[] types, boolean vararg, boolean ths )
	{
		// Initialize all used values from the context
		Object[] args = context.getArgs();
		Class[] argTypes = context.getArgTypes();

		int argCount = args.length;
		int typeCount = types.length;
		int lastType = typeCount - 1;
		int start = ths ? 1 : 0;

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
			int difficulty = 0;
			for( int i = start; i < argCount; i++ )
			{
				int ret = Types.assignable( args[ i ], types[ i ] );
				if( ret < 0 )
					return null;
				difficulty += ret;
			}
			return new MethodCall( false, difficulty, args );
		}

		// Varargs

		int difficulty = 0;
		for( int i = start; i < lastType; i++ )
		{
			int ret = Types.assignable( args[ i ], types[ i ] );
			if( ret < 0 )
				return null;
			difficulty += ret;
		}

		if( ths && lastType == 0 )
			throw new GroovyRuntimeException( "Can't have vararg and includesThis, and lastType == 0" );

		// Adapt the arguments to the method

		Object[] newArgs = new Object[ typeCount ];
		System.arraycopy( args, 0, newArgs, 0, lastType );
		int diff = argCount - lastType;
		Class componentType = types[ lastType ].getComponentType();
		Object[] varArgs = new Object[ diff ];
		for( int i = 0; i < diff; i++ )
		{
			int ret = Types.assignable( args[ i + lastType ], componentType );
			if( ret < 0 )
				return null;
			difficulty += ret;
			varArgs[ i ] = args[ i + lastType ];
		}

		newArgs[ lastType ] = varArgs;
		return new MethodCall( true, difficulty, newArgs );
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
//		NestingLogger.message( "Step 1, determining conversionless candidates, from " + best.size() + " candidates" );

		List< MethodCall > best2 = new ArrayList<MethodCall>( best );
		int least = best2.get( 0 ).difficulty;
		for( MethodCall methodCall : best2 )
		{
			if( methodCall.difficulty < least )
				least = methodCall.difficulty;
		}

		for( Iterator iterator = best2.iterator(); iterator.hasNext(); )
		{
			MethodCall candidate = (MethodCall)iterator.next();
			if( candidate.difficulty > least )
				iterator.remove();
		}

		if( best2.size() == 1 )
			return best2.get( 0 );

		if( best2.size() > 0 )
			best = best2;

		// --------------------
//		NestingLogger.message( "Step 3, determining no-vararg candidates, from " + best.size() + " candidates" );

		best2 = new ArrayList( best );
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
//		best2 = new ArrayList( best );
//		int index = 0;
//		while( index < best2.size() - 1 )
//		{
//			MethodCall candidate1 = best2.get( index );
//			MethodCall candidate2 = best2.get( index + 1 );
//			int ret = compareNonConvertingCalls( candidate1.getParameterTypes(), candidate1.getArgs(), candidate1.isVarargCall, candidate2.getParameterTypes(), candidate2.isVarargCall );
//			if( ret < 0 ) // candidate1 is not assignable to candidate2
//			{
//				best2.remove( index );
//				if( index > 0 )
//					index--;
//			}
//			else if( ret == 0 ) // identical
////					if( candidate1.precedence > candidate2.precedence )
////					{
////						best2.remove( index );
////						if( index > 0 )
////							index--;
////					}
////					else
//					best2.remove( index + 1 );
//			else if( ret == 1 )
//				best2.remove( index + 1 );
//			else
////				if( candidate1.precedence < candidate2.precedence )
////					best2.remove( index + 1 );
////				else if( candidate1.precedence > candidate2.precedence )
////				{
////					best2.remove( index );
////					if( index > 0 )
////						index--;
////				}
////				else
//					index++;
//		}
//
//		if( best2.size() == 1 )
//			return best2.get( 0 );
//
//		if( best2.size() > 0 )
//			best = best2;
//
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


	/**
	 * @return -1 if call2 is assignable to call1, 0 if calls are identical, 1 if call1 is assignable to call2, 2 if not comparable.
	 */
	// Only non conversion compare
	// TODO (RMB) Is now also called for "converting calls". Should we add an if?
	// TODO (RMB) If we make MethodCall & ConstructorCall related then we can pass that to this method.
	static public int compareNonConvertingCalls( Class[] call1, Object[] args1, boolean call1VarArgCall, Class[] call2, boolean call2VarArgCall )
	{
		// TODO (RMB) What if both varargs are not used by the call? They are still checked, not good? We need arg count too.

		int i1 = /* call1IncludesThis ? 1 : */ 0;
		int i2 = /* call2IncludesThis ? 1 : */ 0;

		boolean call1bigger = false;
		boolean call2bigger = false;
		boolean ignoredArg = false;

		boolean varArg1 = false;
		boolean varArg2 = false;

		Class type1 = null;
		Object arg1 = null;
		Class type2 = null;

		int last1 = call1.length - 1;
		int last2 = call2.length - 1;

		if( call1VarArgCall && i1 == last1 ) // Are we on the last argument?
		{
			type1 = call1[ i1 ].getComponentType();
			arg1 = args1[ i1 ];
			varArg1 = true;
		}

		if( call2VarArgCall && i2 == last2 ) // Are we on the last argument?
		{
			type2 = call2[ i2 ].getComponentType();
			varArg2 = true;
		}

		if( !varArg1 )
		{
			if( i1 > last1 ) // Are we over the last argument?
			{
				if( varArg2 )
					return 1;
				if( i2 > last2 )
					return 0;
				throw Assert.fail();
			}
			type1 = call1[ i1 ];
			arg1 = args1[ i1 ];
		}

		if( !varArg2 )
		{
			if( i2 > last2 ) // Are we over the last argument?
			{
				if( varArg1 )
					return -1; // call2 is bigger then call1 (call1 has an unused vararg)
				return 1; // call1 is bigger then call2 (call1 has more arguments)
			}
			type2 = call2[ i2 ];
		}

		while( true )
		{
			// TODO (RMB) Should we add caching here or caching for this complete method?
			int ret1 = Types.compareSpecificness( type1, type2 );
			if( ret1 != 0 )
			{
				if( arg1 == null )
					ignoredArg = true; // Don't ignore if they are equal.
				else if( ret1 < 0 )
				{
					if( call1bigger )
						return 2; // Not comparable
					call2bigger = true;
				}
				else if( ret1 == 2 )
				{
					return 2; // Not comparable
				}
				else if( ret1 == 1 )
				{
					if( call2bigger )
						return 2; // Not comparable
					call1bigger = true;
				}
			}

			// Now we jump to the next parameters

			if( varArg1 )
			{
				if( varArg2 )
					break; // Both on the last
			}
			else
			{
				i1++;
				if( i1 > last1 )
				{
					type1 = null; // No more
				}
				else if( call1VarArgCall && i1 == last1 )
				{
					type1 = call1[ i1 ].getComponentType(); // On the vararg
					arg1 = "whatever";
					varArg1 = true;
				}
				else
				{
					type1 = call1[ i1 ]; // Normal
					arg1 = args1[ i1 ];
				}
			}

			if( !varArg2 )
			{
				i2++;
				if( i2 > last2 )
				{
					type2 = null; // No more
				}
				else if( call2VarArgCall && i2 == last2 )
				{
					type2 = call2[ i2 ].getComponentType(); // On the vararg
					varArg2 = true;
				}
				else
					type2 = call2[ i2 ]; // Normal
			}

			// Are we finished?

			if( type1 == null )
			{
				if( varArg2 || type2 == null )
					break;
				throw Assert.fail();
			}
			else if( type2 == null )
			{
				if( varArg1 )
					break;
				throw Assert.fail();
			}
		}

		if( call1bigger )
			return 1; // call1 is assignable to call2
		if( call2bigger )
			return -1; // call2 is assignable to call1
		if( call1VarArgCall )
			if( call2VarArgCall )
			{
				if( last1 > last2 )
					return 1;
				if( last1 < last2 )
					return -1;
				if( ignoredArg )
					return 2;
				return 0;
			}
			else
				return -1;
		else
			if( call2VarArgCall )
				return 1;
		if( ignoredArg )
			return 2;
		return 0;
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
