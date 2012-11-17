package solidstack.script.objects;

import java.util.HashMap;
import java.util.Map;

public class Symbols
{
	static private Map<String,Symbol> symbols = new HashMap<String, Symbol>(); // TODO Use the ValueMap

	public static Symbol forString( String name )
	{
		Symbol symbol = symbols.get( name );
		if( symbol != null )
			return symbol;

		symbol = new Symbol( name );
		symbols.put( name, symbol );
		return symbol;
	}
}
