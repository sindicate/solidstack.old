package solidstack.script;

public class Returning extends RuntimeException
{
	private Object value;

	public Returning( Object value )
	{
		this.value = value;
	}

	@Override
	public synchronized Throwable fillInStackTrace()
	{
		return null;
	}

	public Object getValue()
	{
		return this.value;
	}
}
