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
	public Ref findRef( Symbol symbol )
	{
		return new MapRef( symbol.toString() );
	}

	@Override
	public Variable def( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	public class MapRef implements Ref
	{
		private String key;

		public MapRef( String key )
		{
			this.key = key;
		}

		public Object get()
		{
			return MapScope.this.map.get( this.key );
		}

		public void set( Object value )
		{
			MapScope.this.map.put( this.key, value );
		}

		public boolean isUndefined()
		{
			return MapScope.this.map.containsKey( this.key );
		}

		public Symbol getKey()
		{
			return Symbol.apply( this.key );
		}
	}
}
