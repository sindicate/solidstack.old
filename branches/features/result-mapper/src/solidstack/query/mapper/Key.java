package solidstack.query.mapper;

import java.util.Arrays;


/**
 * A multi-valued key.
 */
public class Key
{
	private Object[] values;

	/**
	 * @param values The key values.
	 */
	public Key( Object[] values )
	{
		if( values == null )
			throw new NullPointerException( "values can't be null" );
		this.values = values;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		for( Object value : this.values )
		{
			result *= 31;
			if( value != null ) result += value.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals( Object other )
	{
		if( !( other instanceof Key ) )
			return false;
		Key key = (Key)other;
		int len = this.values.length;
		if( len != key.values.length )
			return false;
		for( int i = 0; i < len; i++ )
		{
			Object o1 = this.values[ i ];
			Object o2 = key.values[ i ];
			if( o1 == null )
			{
				if( o2 != null )
					return false;
			}
			else
			{
				if( !o1.equals( o2 ) )
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "Key" + Arrays.deepToString( this.values );
	}
}
