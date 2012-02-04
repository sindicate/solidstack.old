package solidstack.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Parameters support class that makes it easier to build up a map of parameters.
 * 
 * @author René de Bloois
 */
public class Par extends HashMap< String, Object >
{
	/**
	 * An empty map.
	 */
	static public final Map< String, Object > EMPTY = Collections.emptyMap();

	/**
	 * Constructor.
	 * 
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 */
	public Par( String name, Object value )
	{
		set( name, value );
	}

	/**
	 * Constructor.
	 * 
	 * @param nameValue Pairs of names and values.
	 */
	public Par( Object... nameValue )
	{
		set( nameValue );
	}

	/**
	 * Set the given parameter.
	 * 
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @return This object so that you can chain set calls.
	 */
	public Par set( String name, Object value )
	{
		put( name, value );
		return this;
	}

	/**
	 * Set the given parameters.
	 * 
	 * @param nameValue Pairs of names and values.
	 * @return This object so that you can chain set calls.
	 */
	public Par set( Object... nameValue )
	{
		int len = nameValue.length;
		if( len % 2 != 0 )
			throw new IllegalArgumentException( "Need an even arg count (name/value pairs)" );
		for( int i = 0; i < len; )
		{
			if( !( nameValue[ i ] instanceof String ) )
				throw new IllegalArgumentException( "Need name/value pairs, name must be String" );
			put( (String)nameValue[ i++ ], nameValue[ i++ ] );
		}
		return this;
	}
}
