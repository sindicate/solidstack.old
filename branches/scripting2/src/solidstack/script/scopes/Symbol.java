package solidstack.script.scopes;

import java.util.HashMap;
import java.util.Map;

public class Symbol
{
	static private Map<String,Symbol> symbols = new HashMap<String, Symbol>(); // TODO Use the ValueMap

	static public Symbol forString( String name )
	{
		Symbol symbol = symbols.get( name );
		if( symbol != null )
			return symbol;

		symbol = new Symbol( name );
		symbols.put( name, symbol );
		return symbol;
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

	@Override
	public boolean equals( Object obj )
	{
		if( super.equals( obj ) )
			return true;
		if( obj instanceof TempSymbol )
			return this.name.equals( ( (Symbol)obj ).name );
		return false;
	}
}
