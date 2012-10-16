package solidstack.script;


public class ObjectAccess
{
	private Object object;
	private String name;

	public ObjectAccess( Object object, String name )
	{
		this.object = object;
		this.name = name;
	}

	public Object invoke( Object... args )
	{
		return Java.invoke( this.object, this.name, args );
	}
}
