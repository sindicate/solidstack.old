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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import solidstack.script.Script;
import solidstack.script.objects.Assoc;
import solidstack.script.objects.FunnyString;


/**
 * Default Java extensions.
 */
public class DefaultClassExtensions
{
	// TODO: count(), distinct(), filter(), filterNot(), find(), findIndexOf(), fold(), forall(), foreach(), groupBy(), intersect(), map(), partition(), reduce(), reverse(), reverseMap()

	// TODO Should be covered by the integer version
	static public Integer abs( Byte _byte )
	{
		return Math.abs( _byte );
	}

	// TODO Should be covered by the integer version
	static public Integer abs( Character _char )
	{
		return Math.abs( _char );
	}

	static public Double abs( Double _double )
	{
		return Math.abs( _double );
	}

	static public Float abs( Float _float )
	{
		return Math.abs( _float );
	}

	static public Integer abs( Integer _int )
	{
		return Math.abs( _int );
	}

	static public Long abs( Long _long )
	{
		return Math.abs( _long );
	}

	// TODO Should be covered by the integer version
	static public Integer abs( Short _short )
	{
		return Math.abs( _short );
	}

	static public StringBuilder addString( Iterable iterable, StringBuilder buf )
	{
		return addString( iterable.iterator(), buf, "", "", "" );
	}

	static public StringBuilder addString( Iterable iterable, StringBuilder buf, String separator )
	{
		return addString( iterable.iterator(), buf, "", separator, "" );
	}

	static public StringBuilder addString( Iterable iterable, StringBuilder buf, String start, String separator, String end )
	{
		return addString( iterable.iterator(), buf, start, separator, end );
	}

	static public StringBuilder addString( Iterator iterator, StringBuilder buf )
	{
		return addString( iterator, buf, "", "", "" );
	}

	static public StringBuilder addString( Iterator iterator, StringBuilder buf, String separator )
	{
		return addString( iterator, buf, "", separator, "" );
	}

	static public StringBuilder addString( Iterator iterator, StringBuilder buf, String start, String separator, String end )
	{
		buf.append( start );
		if( iterator.hasNext() )
			buf.append( iterator.next() );
		while( iterator.hasNext() )
		{
			buf.append( separator );
			buf.append( iterator.next() );
		}
		buf.append( end );
		return buf;
	}

	static public StringBuilder addString( Object[] array, StringBuilder buf )
	{
		return addString( array, buf, "", "", "" );
	}

	static public StringBuilder addString( Object[] array, StringBuilder buf, String separator )
	{
		return addString( array, buf, "", separator, "" );
	}

	static public StringBuilder addString( Object[] array, StringBuilder buf, String start, String separator, String end )
	{
		buf.append( start );
		int len = array.length;
		if( len > 0 )
			buf.append( array[ 0 ] );
		for( int i = 1; i < len; i++ )
		{
			buf.append( separator );
			buf.append( array[ i ] );
		}
		buf.append( end );
		return buf;
	}

	static public List static_apply( LinkedList list, Object... objects )
	{
		return new LinkedList( Arrays.asList( objects ) );
	}

	static public Class static_apply( Class dummy, String name ) throws ClassNotFoundException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
//		try
//		{
			return Java.forName( name, loader );
//		}
//		catch( ClassNotFoundException e )
//		{
//			throw new ThrowException( "No such class: " + e.getMessage(), ThreadContext.get().cloneStack() );
//		}
	}

	static public List static_apply( List list, Object... objects )
	{
		return new ArrayList( Arrays.asList( objects ) );
	}

	static public Map static_apply( Map map, Assoc... entries )
	{
		HashMap result = new HashMap();
		for( Assoc labeled : entries )
			result.put( labeled.getLabel(), labeled.getValue() );
		return result;
	}

	static public Set static_apply( Set set, Object... objects )
	{
		return new HashSet( Arrays.asList( objects ) );
	}

	static public Object apply( List list, int index )
	{
		return list.get( index );
	}

	static public Object apply( Map map, Object key )
	{
		return map.get( key );
	}

	static public Object apply( Object[] array, int index )
	{
		return array[ index ];
	}

	static public List filter( Iterable iterable, Function function )
	{
		return filter( iterable.iterator(), function );
	}

	static public List filter( Iterator iterator, Function function )
	{
		List result = new ArrayList();
		while( iterator.hasNext() )
		{
			Object object = iterator.next();
			if( Script.isTrue( function.call( object ) ) )
				result.add( object );
		}
		return result;
	}

	static public Object fold( Iterable iterable, Object start, Function function )
	{
		return fold( iterable.iterator(), start, function );
	}

	static public Object fold( Iterator iterator, Object start, Function function )
	{
		while( iterator.hasNext() )
			start = function.call( start, iterator.next() );
		return start;
	}

	static public Object foldLeft( Iterable iterable, Object start, Function function )
	{
		return fold( iterable.iterator(), start, function );
	}

	static public Object foldLeft( Iterator iterator, Object start, Function function )
	{
		return fold( iterator, start, function );
	}

	static public Object foreach( Iterable iterable, Function function )
	{
		return foreach( iterable.iterator(), function );
	}

	static public Object foreach( Iterator iterator, Function function )
	{
		// TODO Or should the ThreadContext be a parameter too?
		int count = function.getParameters().length;
		if( count == 2 )
		{
			Object result = null;
			int index = 0;
			while( iterator.hasNext() )
				result = function.call( index++, iterator.next() );
			return result;
		}
		Object result = null;
		while( iterator.hasNext() )
			result = function.call( iterator.next() );
		return result;
	}

	static public Object foreach( Map<?,?> map, Function function )
	{
		Object result = null;
		for( Entry<?,?> entry : map.entrySet() )
			result = function.call( entry.getKey(), entry.getValue() );
		return result;
	}

	static public Object foreachKey( Map<?,?> map, Function function )
	{
		return foreach( map.keySet(), function );
	}

	static public Object foreachValue( Map<?,?> map, Function function )
	{
		return foreach( map.values(), function );
	}

	static public List map( Iterable iterable, Function function )
	{
		return map( iterable.iterator(), function );
	}

	static public List map( Iterator iterator, Function function )
	{
		List result = new ArrayList();
		while( iterator.hasNext() )
			result.add( function.call( iterator.next() ) );
		return result;
	}

	static public List map( Object[] array, Function function )
	{
		List result = new ArrayList(array.length);
		for( Object object : array )
			result.add( function.call( object ) );
		return result;
	}

	static public String mkString( Iterable iterable )
	{
		return mkString( iterable.iterator(), "", "", "" );
	}

	static public String mkString( Iterable iterable, String separator )
	{
		return mkString( iterable.iterator(), "", separator, "" );
	}

	static public String mkString( Iterable iterable, String start, String separator, String end )
	{
		return mkString( iterable.iterator(), start, separator, end );
	}

	static public String mkString( Iterator iterator )
	{
		return mkString( iterator, "", "", "" );
	}

	static public String mkString( Iterator iterator, String separator )
	{
		return mkString( iterator, "", separator, "" );
	}

	static public String mkString( Iterator iterator, String start, String separator, String end )
	{
		return addString( iterator, new StringBuilder(), start, separator, end ).toString();
	}

	static public String mkString( Object[] array )
	{
		return mkString( array, "", "", "" );
	}

	static public String mkString( Object[] array, String separator )
	{
		return mkString( array, "", separator, "" );
	}

	static public String mkString( Object[] array, String start, String separator, String end )
	{
		StringBuilder buf = new StringBuilder();
		addString( array, buf, start, separator, end );
		return buf.toString();
	}

	static public int size( Object[] array )
	{
		return array.length;
	}

	static public int size( boolean[] array )
	{
		return array.length;
	}

	static public int size( char[] array )
	{
		return array.length;
	}

	static public int size( byte[] array )
	{
		return array.length;
	}

	static public int size( short[] array )
	{
		return array.length;
	}

	static public int size( int[] array )
	{
		return array.length;
	}

	static public int size( long[] array )
	{
		return array.length;
	}

	static public int size( float[] array )
	{
		return array.length;
	}

	static public int size( double[] array )
	{
		return array.length;
	}

	static public int size( String string )
	{
		return string.length();
	}

	static public final Pattern STRIPMARGIN_PATTERN = Pattern.compile( "(?m)^[ \\t]*\\|" ); // TODO Why not \s for whitespace?

	static public String stripMargin( String string )
	{
		return STRIPMARGIN_PATTERN.matcher( string ).replaceAll( "" );
	}

	static public String stripMargin( FunnyString string )
	{
		return stripMargin( string.toString() );
	}

	static public Object update( List list, int index, Object value )
	{
		if( index >= list.size() )
		{
			// TODO Is this what we want?
			while( index > list.size() )
				list.add( null );
			list.add( value );
			return value;
		}
		list.set( index, value );
		return value;
	}

	static public Object update( Map map, Object key, Object value )
	{
		map.put( key, value );
		return value;
	}

	static public Object update( Object[] array, int index, Object value )
	{
		array[ index ] = value;
		return value;
	}
}
