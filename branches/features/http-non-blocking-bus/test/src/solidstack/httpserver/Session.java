package solidstack.httpserver;

import java.util.HashMap;
import java.util.Map;

import solidstack.nio.DebugId;

public class Session
{
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public void setAttribute( String name, Object value )
	{
		Loggers.httpServer.debug( "setAttribute, session: {}, {}", DebugId.getId( this ), name );
		this.attributes.put( name, value );
	}

	public Object getAttribute( String name )
	{
		Loggers.httpServer.debug( "getAttribute, session: {}, {}", DebugId.getId( this ), name );
		return this.attributes.get( name );
	}
}
