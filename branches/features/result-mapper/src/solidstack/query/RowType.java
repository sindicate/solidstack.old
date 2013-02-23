package solidstack.query;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Represents the type of a row. It contains the name of the type and the names of the attributes.
 */
public class RowType implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private Map<String,Integer> names;

	/**
	 * @param names The names of the attributes.
	 */
	public RowType( String[] names )
	{
		Map<String,Integer> n = this.names = new LinkedHashMap<String,Integer>();
		for( int len = names.length, i = 0; i < len; i++ )
			n.put( names[ i ], i );
	}

	/**
	 * @param name The name of the type.
	 * @param names The names of the attributes.
	 */
	public RowType( String name, String[] names )
	{
		this( names );
		this.name = name;
	}

	/**
	 * @return The name of the type.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return A map of names to indexes into the tuples.
	 */
	public Map<String,Integer> getAttributeIndex()
	{
		return this.names;
	}
}
