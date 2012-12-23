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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import solidstack.lang.Assert;


public class Types
{
	// [ Argument value's type (vertical) ][ Argument type (horizontal) ]
	// 0: identical, <=100 assignable, <=200 convertable
//    static public final int[][] PRIMITIVE_DISTANCES =
//    {
//    	// TODO (RMB) The conversion distances > 100 seem to be in the wrong order. Redesign all.
//		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, 20 }, // boolean
//		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, 20 }, // Boolean
//		{ 107, 108,   0,   0, 102, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 20 }, // char
//		{ 107, 108,   0,   0, 102, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 20 }, // Character
//		{ 107, 108, 109, 110,   0,   0,   3,   4,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // byte
//		{ 107, 108, 109, 110,   0,   0,   3,   4,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Byte
//		{ 107, 108, 109, 110, 105, 106,   0,   0,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // short
//		{ 107, 108, 109, 110, 105, 106,   0,   0,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Short
//		{ 107, 108, 109, 110, 105, 106, 103, 104,   0,   0,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // int
//		{ 107, 108, 109, 110, 105, 106, 103, 104,   0,   0,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Integer
//		{ 107, 108, 109, 110, 105, 106, 103, 104, 101, 102,   0,   0,   8,  10,  11,  12,  13,   9,   7, 20 }, // long
//		{ 107, 108, 109, 110, 105, 106, 103, 104, 102, 102,   0,   0,   8,  10,  11,  12,  13,   9,   7, 20 }, // Long
//		{ 107, 108, 109, 110,   9,  10,   7,   8,   5,   6,   3,   4,   0,  13,  14,  11,  12,   1,   2, 20 }, // BigInteger
//		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105,   0,   0,   1,   2,   3,   4, 20 }, // float
//		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105,   0,   0,   1,   2,   3,   4, 20 }, // Float
//		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104,   0,   0,   1,   2, 20 }, // double
//		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104,   0,   0,   1,   2, 20 }, // Double
//		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104, 101, 102,   0,   1, 20 }, // BigDecimal
//		{ 101, 102, 114, 115, 114, 115, 112, 113, 110, 111, 108, 109, 107, 105, 106, 103, 104, 102,   0, 20 }, // Number
//		{ 101, 102,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  0 }, // Object
//	};

    // -1 = not, 0 = nop, 1 = easy, 2 = difficult, 3 = depends
    static public final int[][] PRIMITIVE_ASSIGNABILITY =
    {
		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  1 }, // boolean
		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  1 }, // Boolean
		{  -1,  -1,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // char
		{  -1,  -1,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Character
		{  -1,  -1,   1,   1,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // byte
		{  -1,  -1,   1,   1,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Byte
		{  -1,  -1,   3,   3,   3,   3,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // short
		{  -1,  -1,   3,   3,   3,   3,   0,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Short
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   0,   0,   1,   1,   1,   3,   3,   1,   1,   1,   1,  1 }, // int
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   0,   0,   1,   1,   1,   3,   3,   1,   1,   1,   1,  1 }, // Integer
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   3,   3,   0,   0,   1,   3,   3,   3,   3,   1,   1,  1 }, // long
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   3,   3,   0,   0,   1,   3,   3,   3,   3,   1,   1,  1 }, // Long
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   0,   3,   3,   3,   3,   1,   1,  1 }, // BigInteger
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   0,   1,   1,   1,   1,  1 }, // float
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   0,   1,   1,   1,   1,  1 }, // Float
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   0,   1,   1,  1 }, // double
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   0,   1,   1,  1 }, // Double
		{  -1,  -1,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   0,   1,  1 }, // BigDecimal
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,  1 }, // Number
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  0 }, // Object
	};

    // x2 = int, x3 = long, x4 = BI, x5 = float, x6 = double, x7 = BD
    // 1x = convert right, 2x = convert left
    static public final int[][] NUMBER_MATCHING =
    {
		{  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99 }, // boolean
		{  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99 }, // Boolean
		{  99,  99,   1,   1,  12,  12,  12,  12,  12,  12,  13,  13,  14,  15,  15,  16,  16,  17,  99, 99 }, // char
		{  99,  99,   1,   1,  12,  12,  12,  12,  12,  12,  13,  13,  14,  15,  15,  16,  16,  17,  99, 99 }, // Character
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // byte
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // Byte
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // short
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // Short
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // int
		{  99,  99,  22,  22,   2,   2,   2,   2,   2,   2,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // Integer
		{  99,  99,  23,  23,   3,   3,   3,   3,   3,   3,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // long
		{  99,  99,  23,  23,   3,   3,   3,   3,   3,   3,   3,   3,  14,   5,   5,   6,   6,  17,  99, 99 }, // Long
		{  99,  99,  24,  24,  24,  24,  24,  24,  24,  24,  24,  24,   4,   5,   5,   6,   6,  17,  99, 99 }, // BigInteger
		{  99,  99,  25,  25,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   6,   6,  17,  99, 99 }, // float
		{  99,  99,  25,  25,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   6,   6,  17,  99, 99 }, // Float
		{  99,  99,  26,  26,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,  17,  99, 99 }, // double
		{  99,  99,  26,  26,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,  17,  99, 99 }, // Double
		{  99,  99,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,   7,  99, 99 }, // BigDecimal
		{  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99 }, // Number
		{  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99 }, // Object
	};

	static public final Class[] PRIMITIVES =
    {
    	boolean.class, Boolean.class, char.class, Character.class, byte.class, Byte.class, short.class, Short.class,
    	int.class, Integer.class, long.class, Long.class, BigInteger.class,
		float.class, Float.class, double.class, Double.class, BigDecimal.class,
		Number.class, Object.class
	};

    static public final Map< Class, Integer > TYPES;

    static
    {
    	TYPES = new IdentityHashMap<Class, Integer>();
    	for( int i = 0; i < PRIMITIVES.length; i++ )
    		TYPES.put( PRIMITIVES[ i ], i );
    }

    // TODO (RMB) Better map implementation?
    // TODO (RMB) Specificity cache?
    static public final Map< Class, Map< Class, Integer > > distanceCache = new IdentityHashMap< Class, Map<Class,Integer> >();


	static public Class[] getTypes( Object[] objects )
	{
		Class[] result = new Class[ objects.length ];
		for( int i = 0; i < objects.length; i++ )
		{
			Object arg = objects[ i ];
			if( arg != null )
				result[ i ] = arg.getClass();
		}
		return result;
	}

	static public Number toNumber( Object object )
	{
		if( object instanceof Number )
			return (Number)object;
		if( object instanceof Character )
			return Integer.valueOf( ( (Character)object ).charValue() );
		// TODO String to number?
		throw new ClassCastException( object.getClass().getName() + " cannot be cast to java.lang.Number" );
	}

	static public boolean castToBoolean( Object object )
	{
		if( object == null )
			return false;
		if( object instanceof Boolean )
			return (Boolean)object;
		if( object instanceof String )
			return ( (String)object ).length() != 0;
		if( object instanceof Collection )
			return !( (Collection<?>)object ).isEmpty();
		if( object instanceof Map )
			return !( (Map<?,?>)object ).isEmpty();
		return true;
		// TODO Maybe call asBoolean()
	}

	static public char castToChar( Object object )
	{
		if( object instanceof Character )
			return ( (Character)object ).charValue();
		if( object instanceof Number )
			return (char)( (Number)object ).intValue();
		String text = object.toString();
		if( text.length() == 1 )
			return text.charAt( 0 );
		throw new ClassCastException( object.getClass().getName() + " cannot be cast to char" );
	}

	static public Collection asCollection( Object value )
	{
		if( value == null )
			return Collections.EMPTY_LIST;
		if( value instanceof Collection )
			return (Collection)value;
		if( value instanceof Map )
			return ( (Map)value ).entrySet();
		if( value.getClass().isArray() )
		{
			if( value.getClass().getComponentType().isPrimitive() )
				return primitiveArrayToList( value );
			return Arrays.asList( (Object[])value );
		}
		// TODO Lines from a file?
		// TODO Enum values?
		return Collections.singletonList( value ); // lets assume its a collection of 1
	}

	/**
	 * Allows conversion of arrays into a mutable List
	 *
	 * @return the array as a List
	 */
	static public List primitiveArrayToList( Object array )
	{
		int size = Array.getLength( array );
		List list = new ArrayList( size );
		for( int i = 0; i < size; i++ )
		{
			Object item = Array.get( array, i );
			if( item != null && item.getClass().isArray() && item.getClass().getComponentType().isPrimitive() )
				item = primitiveArrayToList( item );
			list.add( item );
		}
		return list;
	}

//    // ATTENTION: The methods isAssignable & compare & calculateDistance & convert need to stay synchronized



	/**
	 * Returns -1 when not, 0 is nop, 1 is easy.
	 *
	 * @param arg
	 * @param type
	 * @return
	 */
	// SYNC isAssignableToType
	static public boolean assignable( Class arg, Class type )
	{
		if( arg == null )
			return !type.isPrimitive();
		if( type.isAssignableFrom( arg ) )
			return true;

		Integer i = TYPES.get( type );
		if( i != null )
		{
			Integer j = TYPES.get( arg );
			if( j != null )
			{
				int a = PRIMITIVE_ASSIGNABILITY[ j ][ i ];
				if( a == 0 || a == 1 )
					return true;
			}
		}

        return false;
	}

	// SYNC isAssignableToType()
	static public Object convert( Object object, Class type )
	{
		if( type.isPrimitive() )
		{
			if( type == boolean.class )
				return castToBoolean( object );

			if( object == null )
				return null; // TODO Or should we throw a NullPointerException?

			if( type == int.class )
				return object instanceof Integer ? object : toNumber( object ).intValue();
			if( type == long.class )
				return object instanceof Long ? object : toNumber( object ).longValue();
			if( type == double.class )
			{
				if( object instanceof Double )
					return object;
				double d = toNumber( object ).doubleValue();
				if( d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY )
					throw new IllegalArgumentException( "Value " + object + " is out of range for a double" );
				return d;
			}
			if( type == byte.class )
				return object instanceof Byte ? object : Byte.valueOf( toNumber( object ).byteValue() );
			if( type == char.class )
				return object instanceof Character ? object : castToChar( object );
			if( type == short.class )
				return object instanceof Short ? object : toNumber( object ).shortValue();
			if( type == float.class )
			{
				if( object instanceof Float )
					return object;
				float f = toNumber( object ).floatValue();
				if( f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY )
					throw new IllegalArgumentException( "Value " + object + " is out of range for a float" );
				return f;
			}

			throw new ClassCastException( object.getClass().getName() + " cannot be cast to " + type.getName() );
        }

		if( object == null || type.isInstance( object ) )
			return object;

		if( Number.class.isAssignableFrom( type ) )
		{
			try // TODO RMB Should we make this try bigger?
			{
				// TODO (RMB) We should analyze the most efficient order of all these if's, or maybe use hashmap that translates Class -> int constant to be used by switch
				if( type == BigDecimal.class )
				{
					if( object instanceof BigInteger )
						return new BigDecimal( (BigInteger)object );
					if( object instanceof Float || object instanceof Double )
						return new BigDecimal( ( (Number)object ).doubleValue() ); // valueOf() behaves differently from new()
					return BigDecimal.valueOf( toNumber( object ).longValue() );
				}
				if( type == BigInteger.class )
				{
					if( object instanceof BigDecimal )
						return ( (BigDecimal)object ).toBigInteger();
					if( object instanceof Float || object instanceof Double )
						return new BigDecimal( ( (Number)object ).doubleValue() ).toBigInteger(); // valueOf() behaves differently from new()
					return BigInteger.valueOf( toNumber( object ).longValue() );
				}
				if( type == Integer.class )
					return toNumber( object ).intValue();
				if( type == Long.class )
					return toNumber( object ).longValue();
				if( type == Double.class )
				{
					double d = toNumber( object ).doubleValue();
					if( d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY )
						throw new IllegalArgumentException( "Value " + object + " is out of range for a double" );
					return d;
				}
				if( type == Byte.class )
					return toNumber( object ).byteValue();
				if( type == Short.class )
					return toNumber( object ).shortValue();
				if( type == Float.class )
				{
					float f = toNumber( object ).floatValue();
					if( f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY )
						throw new IllegalArgumentException( "Value " + object + " is out of range for a float" );
					return f;
				}
			}
			catch( ClassCastException e )
			{
			}

			throw new ClassCastException( object.getClass().getName() + " cannot be cast to " + type.getName() );
		}

        if( type == String.class )
			return object.toString();

		if( type == Boolean.class )
			return castToBoolean( object ); // always succeeds

//		if( type == Long.class )
//		{
//			// TODO (RMB) What's faster? instanceof or getClass() == for final classes?
//			if( object instanceof Integer )
//				return Long.valueOf( (Integer)object );
//		}

		if( type == Object[].class )
			if( object instanceof Collection )
				return ((Collection)object).toArray();

		if( type == List.class )
		{
        	if( object instanceof Collection )
        		return new ArrayList( (Collection)object );

			if( object.getClass().isArray() )
			{
				if( object.getClass().getComponentType().isPrimitive() )
				{
					int length = Array.getLength( object );
					List result = new ArrayList( length );
					for( int i = 0; i < length; i++ )
						result.add( Array.get( object, i ) );
					return result;
				}
				// Need to wrap in real List, the asList one is not mutable
				List result = new ArrayList( Arrays.asList( (Object[])object ) );
				return result;
			}
		}

        if( type == Set.class )
        	if( object instanceof Collection )
        		return new HashSet( (Collection)object );

		if( type == LinkedList.class )
		{
        	if( object instanceof Collection )
        		return new LinkedList( (Collection)object );

			if( object.getClass().isArray() )
			{
				// No better way for a LinkedList
				int length = Array.getLength( object );
				List result = new LinkedList();
				for( int i = 0; i < length; i++ )
					result.add( Array.get( object, i ) );
				return result;
			}
		}

		if( type == Character.class || type == char.class ) // TODO (RMB) I hate this one
			return castToChar( object );

        if( type.isArray() )
		{
			Collection list = asCollection( object );
			Class elementType = type.getComponentType();
			Object array = Array.newInstance( elementType, list.size() );
			int idx = 0;

			if( elementType == boolean.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setBoolean( array, idx, castToBoolean( iter.next() ) );

			else if( elementType == byte.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setByte( array, idx, toNumber( iter.next() ).byteValue() );

			else if( elementType == char.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setChar( array, idx, castToChar( iter.next() ) );

			else if( elementType == double.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setDouble( array, idx, toNumber( iter.next() ).doubleValue() );

			else if( elementType == float.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setFloat( array, idx, toNumber( iter.next() ).floatValue() );

			else if( elementType == int.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setInt( array, idx, toNumber( iter.next() ).intValue() );

			else if( elementType == long.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setLong( array, idx, toNumber( iter.next() ).longValue() );

			else if( elementType == short.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setShort( array, idx, toNumber( iter.next() ).shortValue() );

			else
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.set( array, idx, convert( iter.next(), elementType ) );

			return array;
		}

        // TODO Cast to class?

		throw new ClassCastException( object.getClass().getName() + " cannot be cast to " + type.getName() );
	}

	static public Object[] match( Object left, Object right )
	{
		Integer i = TYPES.get( left.getClass() );
		if( i != null )
		{
			Integer j = TYPES.get( right.getClass() );
			if( j != null )
			{
				int a = NUMBER_MATCHING[ j ][ i ];
				if( a != 99 )
				{
					switch( a )
					{
						case 1:
							return new Object[] { 2, convert( left, int.class ), convert( right, int.class ) };
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
							return new Object[] { a, left, right };
						case 12:
							return new Object[] { 2, left, convert( right, int.class ) };
						case 13:
							return new Object[] { 3, left, convert( right, long.class ) };
						case 14:
							return new Object[] { 4, left, convert( right, BigInteger.class ) };
						case 15:
							return new Object[] { 5, left, convert( right, float.class ) };
						case 16:
							return new Object[] { 6, left, convert( right, double.class ) };
						case 17:
							return new Object[] { 7, left, convert( right, BigDecimal.class ) };
						case 22:
							return new Object[] { 2, convert( left, int.class ), right };
						case 23:
							return new Object[] { 3, convert( left, long.class ), right };
						case 24:
							return new Object[] { 4, convert( left, BigInteger.class ), right };
						case 25:
							return new Object[] { 5, convert( left, float.class ), right };
						case 26:
							return new Object[] { 6, convert( left, double.class ), right };
						case 27:
							return new Object[] { 7, convert( left, BigDecimal.class ), right };
					}
				}
			}
		}

		throw Assert.fail();
	}

	static public Object[] transformArguments( Class[] types, Object[] args )
	{
		Object[] result = new Object[ args.length ];
		for( int i = types.length - 1; i >= 0; i-- )
			result[ i ] = Types.convert( args[ i ], types[ i ] );
		return result;
	}
}
