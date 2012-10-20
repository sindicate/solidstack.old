package solidstack.script;

import solidstack.script.java.Java;


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
