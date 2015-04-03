package solidstack.httpserver;

import java.util.HashMap;
import java.util.Map;

public class Session
{
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public void setAttribute( String name, Object value )
	{
		this.attributes.put( name, value );
	}

	public Object getAttribute( String name )
	{
		return this.attributes.get( name );
	}
}
