package solidstack.script.objects;

public class Symbol
{
	private String name;
	private int hashCode;

	public Symbol( String name )
	{
		this.name = name;
		this.hashCode = name.hashCode();
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

	// No equals() needed only object identity equality
}
