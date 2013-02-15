package solidstack.script.scopes;

import java.util.Map;

import funny.Symbol;

public class MapScope extends AbstractScope
{
	private Map<Object, Object> map;

	public MapScope( Map<Object, Object> map )
	{
		this.map = map;
	}

	@Override
	public Variable var( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get( Symbol symbol )
	{
		Object result = this.map.get( symbol.toString() );
		if( result != null )
			return result;
		if( this.map.containsKey( symbol.toString() ) )
			return null;
		throw new UndefinedException();
	}

	@Override
	protected void set0( Symbol symbol, Object value )
	{
		if( !this.map.containsKey( symbol.toString() ) )
			throw new UndefinedException();
		this.map.put( symbol.toString(), value );
	}

	public Object apply( Symbol symbol, Object... pars )
	{
		throw new UnsupportedOperationException();
	}

	public Object apply( Symbol symbol, Map args )
	{
		throw new UnsupportedOperationException();
	}
}
