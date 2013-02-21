package solidstack.query.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Key
{
	private List<Object> values = new ArrayList<Object>();

	public void add( Object value )
	{
		this.values.add( value );
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
		if( this.values.size() != key.values.size() )
			return false;
		Iterator<Object> i1 = this.values.iterator();
		Iterator<Object> i2 = key.values.iterator();
		while( i1.hasNext() )
		{
			Object o1 = i1.next();
			Object o2 = i2.next();
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
		return this.values.toString();
	}
}
