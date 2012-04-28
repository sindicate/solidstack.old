package solidstack.util;


/**
 * Support for objects.
 *
 * @author René de Bloois
 */
public class Objects
{
	/**
	 * @param o1 The first object.
	 * @param o2 The second object.
	 * @return True if both objects are null or equal, false otherwise.
	 */
	static public boolean equals( Object o1, Object o2 )
	{
		if( o1 == o2 )
			return true;
		if( o1 == null )
			return false;
		return o1.equals( o2 );
	}
}
