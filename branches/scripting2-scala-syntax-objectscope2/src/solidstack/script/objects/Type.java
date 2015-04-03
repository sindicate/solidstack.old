package solidstack.script.objects;

public class Type
{
	private Class type;

	public Type( Class<?> type )
	{
		this.type = type;
	}

	public Class<?> theClass()
	{
		return this.type;
	}
}
