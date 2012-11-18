package solidstack.script.scopes;

import java.util.Map;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		// TODO Auto-generated method stub
		return null;
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
			return new TempSymbol( this.key );
		}
	}
}
