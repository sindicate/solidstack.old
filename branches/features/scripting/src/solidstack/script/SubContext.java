package solidstack.script;

import java.util.HashMap;
import java.util.Map;

public class SubContext extends Context
{
	private Context parent;
	private Map<String, Object> map = new HashMap<String, Object>();

	public SubContext( Context parent )
	{
		this.parent = parent;
	}

	@Override
	public Object get( String name )
	{
		Object result = this.map.get( name );
		if( result == null )
			return this.parent.get( name );
		if( result == Null.INSTANCE )
			return null;
		return result;
	}

	@Override
	public void set( String name, Object value )
	{
		this.map.put( name, value != null ? value : Null.INSTANCE );
	}
}
