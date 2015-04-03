package solidstack.script.scopes;

public class TempSymbol extends Symbol
{
	public TempSymbol( String name )
	{
		super( name );
	}

	public TempSymbol( String name, int hashCode )
	{
		super( name, hashCode );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( super.equals( obj ) )
			return true;
		if( obj instanceof Symbol )
			return this.name.equals( ( (Symbol)obj ).name );
		return false;
	}
}
