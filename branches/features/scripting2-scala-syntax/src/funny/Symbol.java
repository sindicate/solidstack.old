package funny;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class Symbol
{
	static private WeakHashMap<String,WeakReference<Symbol>> symbols = new WeakHashMap<String,WeakReference<Symbol>>();

	static public Symbol apply( String name )
	{
		synchronized( symbols )
		{
			WeakReference<Symbol> symbol = symbols.get( name );
			Symbol result;
			if( symbol != null )
			{
				result = symbol.get();
				if( result != null )
					return result;
				// else: Symbol is lost, and the original key can't be retrieved, so we need to overwrite it completely
				symbols.remove( name ); // Needed because put() will not replace the key if it already exists
			}
			// else: Key is lost or never existed, which means that the symbol does not exist or never existed

			result = new Symbol( name );
			symbols.put( name, new WeakReference<Symbol>( result ) );
			return result;
		}
	}

	String name;
	private int hashCode;

	Symbol( String name )
	{
		this.name = name;
		this.hashCode = name.hashCode();
	}

	Symbol( String name, int hashCode )
	{
		this.name = name;
		this.hashCode = hashCode;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}
}
