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

import solidstack.script.ScriptException;


public class Types
{
	// [ Argument value's type (vertical) ][ Argument type (horizontal) ]
	// 0: identical, <=100 assignable, <=200 convertable
    static public final int[][] PRIMITIVE_DISTANCES =
    {
    	// TODO (RMB) The conversion distances > 100 seem to be in the wrong order. Redesign all.
		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, 20 }, // boolean
		{   0,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, 20 }, // Boolean
		{ 107, 108,   0,   0, 102, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 20 }, // char
		{ 107, 108,   0,   0, 102, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 20 }, // Character
		{ 107, 108, 109, 110,   0,   0,   3,   4,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // byte
		{ 107, 108, 109, 110,   0,   0,   3,   4,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Byte
		{ 107, 108, 109, 110, 105, 106,   0,   0,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // short
		{ 107, 108, 109, 110, 105, 106,   0,   0,   1,   2,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Short
		{ 107, 108, 109, 110, 105, 106, 103, 104,   0,   0,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // int
		{ 107, 108, 109, 110, 105, 106, 103, 104,   0,   0,   5,   6,   8,  10,  11,  12,  13,   9,   7, 20 }, // Integer
		{ 107, 108, 109, 110, 105, 106, 103, 104, 101, 102,   0,   0,   8,  10,  11,  12,  13,   9,   7, 20 }, // long
		{ 107, 108, 109, 110, 105, 106, 103, 104, 102, 102,   0,   0,   8,  10,  11,  12,  13,   9,   7, 20 }, // Long
		{ 107, 108, 109, 110,   9,  10,   7,   8,   5,   6,   3,   4,   0,  13,  14,  11,  12,   1,   2, 20 }, // BigInteger
		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105,   0,   0,   1,   2,   3,   4, 20 }, // float
		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105,   0,   0,   1,   2,   3,   4, 20 }, // Float
		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104,   0,   0,   1,   2, 20 }, // double
		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104,   0,   0,   1,   2, 20 }, // Double
		{ 114, 115, 116, 117, 112, 113, 110, 111, 108, 109, 106, 107, 105, 103, 104, 101, 102,   0,   1, 20 }, // BigDecimal
		{ 101, 102, 114, 115, 114, 115, 112, 113, 110, 111, 108, 109, 107, 105, 106, 103, 104, 102,   0, 20 }, // Number
		{ 101, 102,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  0 }, // Object
	};

    // Symmetric matrix
    // 0 = equal, 1 = assignable, 2 = not assignable
    static public final int[][] PRIMITIVE_ASSIGNABILITY =
    {
		{   0,   1,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  1 }, // boolean
		{  -1,   0,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  1 }, // Boolean
		{   2,   2,   0,   1,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  1 }, // char
		{   2,   2,  -1,   0,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  1 }, // Character
		{   2,   2,   2,   2,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // byte
		{   2,   2,   2,   2,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Byte
		{   2,   2,   2,   2,  -1,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // short
		{   2,   2,   2,   2,  -1,  -1,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Short
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // int
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   1,   1,  1 }, // Integer
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,   0,   1,   1,   1,   1,   2,   2,   1,   1,  1 }, // long
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   1,   1,   1,   2,   2,   1,   1,  1 }, // Long
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   2,   2,   2,   2,   1,   1,  1 }, // BigInteger
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   2,   0,   1,   1,   1,   2,   1,  1 }, // float
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   2,  -1,   0,   1,   1,   2,   1,  1 }, // Float
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   2,   2,  -1,  -1,   0,   1,   2,   1,  1 }, // double
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   2,   2,  -1,  -1,  -1,   0,   2,   1,  1 }, // Double
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   2,   2,   2,   2,   0,   1,  1 }, // BigDecimal
		{   2,   2,   2,   2,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,  1 }, // Number
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  0 }, // Object
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


	// TODO RMB 1.7 has a strange construct here.
	public static Number castToNumber( Object object )
	{
		if( object instanceof Number )
			return (Number)object;
		if( object instanceof Character )
			return new Integer( ( (Character)object ).charValue() );
		if( object instanceof String )
		{
			// TODO (RMB) Remove this ugly string conversion?
			String c = (String)object;
			if( c.length() == 1 )
				return new Integer( c.charAt( 0 ) );
		}
		throw new TypeConversionException( object, Number.class );
	}

    /**
     * Method used for coercing an object to a boolean value,
     * thanks to an <code>asBoolean()</code> method added on types.
     *
     * @param object to coerce to a boolean value
     * @return a boolean value
     */
	public static boolean castToBoolean( Object object )
	{
		if( object == null )
			return false;
		return true;
		// TODO Maybe call asBoolean()
	}

	public static char castToChar( Object object )
	{
		if( object instanceof Character )
			return ( (Character)object ).charValue();
		if( object instanceof Number )
			return (char)( (Number)object ).intValue();
		String text = object.toString();
		if( text.length() == 1 )
			return text.charAt( 0 );
		throw new TypeConversionException( text, char.class );
	}

	public static Collection asCollection( Object value )
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
	public static List primitiveArrayToList( Object array )
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


	// SYNC isAssignableToType
    // TODO (RMB) Maybe we should make String in GString identical (no conversion), return 0
	public static int compare( Class arg, Class type )
	{
//		if( NestingLogger.isEnabled() )
//			NestingLogger.message( "here" );

		if( arg == null )
		{
			if( type.isPrimitive() )
				return -1;
			return 1;
		}

		if( type == arg )
			return 0;

		if( type.isAssignableFrom( arg ) )
			return 1;

		Integer i = TYPES.get( type );
		if( i != null )
		{
			Integer j = TYPES.get( arg );
			if( j != null )
			{
				try
				{
					int distance = PRIMITIVE_DISTANCES[ j ][ i ];
					return distance > 100 ? 2 : distance > 0 ? 1 : distance < 0 ? -1 : 0;
				}
				catch( NullPointerException e )
				{
					throw new ScriptException( "Primitive distance for " + arg.getName() + " and " + type.getName() + " not found" );
				}
			}
		}

		if( type == String.class )
			return 2;

		if( type == Boolean.class || type == boolean.class )
			return 2;

        if( type == Object[].class )
			if( Collection.class.isAssignableFrom( arg ) )
				return 2;

		if( type == List.class )
		{
			if( Collection.class.isAssignableFrom( arg ) )
        		return 2;

			if( arg.isArray() )
        		return 2;
		}

        if( type == Set.class )
			if( Collection.class.isAssignableFrom( arg ) )
        		return 2;

		if( type == LinkedList.class )
		{
			if( Collection.class.isAssignableFrom( arg ) )
        		return 2;

			if( arg.isArray() )
        		return 2;
		}

		if( type == Character.class || type == char.class ) // TODO (RMB) I hate this one
			return 2;

		if( type.isArray() )
			return 2;

        if( type == Class.class )
        	if( arg == String.class /* || special string */ )
        		return 2;

        return -1;
	}


	// SYNC isAssignableToType
    // TODO (RMB) Maybe we should make String in GString identical (no conversion), return 0
	public static int compareSpecificness( Class arg, Class type )
	{
		if( arg == null )
			throw new IllegalArgumentException( "'arg' should not be null" );

		int result = 2;
		if( type == arg )
			result = 0;
		else if( type.isAssignableFrom( arg ) )
			result = 1;
		else if( arg.isAssignableFrom( type ) )
			result = -1;
		else
		{
			Integer i = TYPES.get( type );
			if( i != null )
			{
				Integer j = TYPES.get( arg );
				if( j != null )
					result = PRIMITIVE_ASSIGNABILITY[ j ][ i ];
			}
		}

        return result;
	}


	private static int calculateDistance( Class arg, Class type )
	{
		// arg can never be a primitive

		if( arg == null )
		{
			int distance = 0;
			while( type.isArray() )
			{
				distance++;
				type = type.getComponentType();
			}

			if( type.isInterface() )
				return distance + 1;

			// Determine distance to Object (number of super classes)
			Class superCls = type.getSuperclass();
			while( superCls != null )
			{
				distance++;
				superCls = superCls.getSuperclass();
			}

			return distance;
		}

		Integer i = TYPES.get( type );
		if( i != null )
		{
			Integer j = TYPES.get( arg );
			if( j != null )
				return PRIMITIVE_DISTANCES[ j ][ i ];
		}

		return 0;
	}


	// SYNC isAssignableToType()
	public static Object convert( Object object, Class type )
	{
		if( object == null )
			return null;

		if( type.isInstance( object ) )
			return object;

		if( type.isPrimitive() )
		{
			if( type == int.class )
				return object instanceof Integer ? object : castToNumber( object ).intValue();
			if( type == boolean.class )
				return object instanceof Boolean ? object : castToBoolean( object );
			if( type == long.class )
				return object instanceof Long ? object : castToNumber( object ).longValue();
			if( type == double.class )
			{
				if( object instanceof Double )
					return object;
				double d = castToNumber( object ).doubleValue();
				if( d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY )
					throw new IllegalArgumentException( "Value " + object + " is out of range for a double" );
				return d;
			}
			if( type == byte.class )
				return object instanceof Byte ? object : castToNumber( object ).byteValue();
			if( type == char.class )
				return object instanceof Character ? object : castToChar( object );
			if( type == short.class )
				return object instanceof Short ? object : castToNumber( object ).shortValue();
			if( type == float.class )
			{
				if( object instanceof Float )
					return object;
				float f = castToNumber( object ).floatValue();
				if( f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY )
					throw new IllegalArgumentException( "Value " + object + " is out of range for a float" );
				return f;
			}

			throw new TypeConversionException( object, type );
        }

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
					return BigDecimal.valueOf( castToNumber( object ).longValue() );
				}
				if( type == BigInteger.class )
				{
					if( object instanceof BigDecimal )
						return ( (BigDecimal)object ).toBigInteger();
					if( object instanceof Float || object instanceof Double )
						return new BigDecimal( ( (Number)object ).doubleValue() ).toBigInteger(); // valueOf() behaves differently from new()
					return BigInteger.valueOf( castToNumber( object ).longValue() );
				}
				if( type == Integer.class )
					return castToNumber( object ).intValue();
				if( type == Long.class )
					return castToNumber( object ).longValue();
				if( type == Double.class )
				{
					double d = castToNumber( object ).doubleValue();
					if( d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY )
						throw new IllegalArgumentException( "Value " + object + " is out of range for a double" );
					return d;
				}
				if( type == Byte.class )
					return castToNumber( object ).byteValue();
				if( type == Short.class )
					return castToNumber( object ).shortValue();
				if( type == Float.class )
				{
					float f = castToNumber( object ).floatValue();
					if( f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY )
						throw new IllegalArgumentException( "Value " + object + " is out of range for a float" );
					return f;
				}
			}
			catch( TypeConversionException e )
			{
			}

			throw new TypeConversionException( object, type );
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
					Array.setByte( array, idx, castToNumber( iter.next() ).byteValue() );

			else if( elementType == char.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setChar( array, idx, castToChar( iter.next() ) );

			else if( elementType == double.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setDouble( array, idx, castToNumber( iter.next() ).doubleValue() );

			else if( elementType == float.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setFloat( array, idx, castToNumber( iter.next() ).floatValue() );

			else if( elementType == int.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setInt( array, idx, castToNumber( iter.next() ).intValue() );

			else if( elementType == long.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setLong( array, idx, castToNumber( iter.next() ).longValue() );

			else if( elementType == short.class )
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.setShort( array, idx, castToNumber( iter.next() ).shortValue() );

			else
				for( Iterator iter = list.iterator(); iter.hasNext(); idx++ )
					Array.set( array, idx, convert( iter.next(), elementType ) );

			return array;
		}

        // TODO Cast to class?

		throw new TypeConversionException( object, type );
	}

    static public int getDistance( Class arg, Class type )
	{
		Map< Class, Integer > cache;
		synchronized( distanceCache )
		{
			cache = distanceCache.get( type );
			if( cache == null )
				distanceCache.put( type, cache = new IdentityHashMap< Class, Integer >() );
		}
		int result;
		synchronized( cache )
		{
			Integer distance = cache.get( arg );
			if( distance == null )
			{
				distance = calculateDistance( arg, type );
				cache.put( arg, distance );
			}
			result = distance;
		}

		return result;
	}
}
