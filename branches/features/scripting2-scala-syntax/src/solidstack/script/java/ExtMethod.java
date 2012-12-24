package solidstack.script.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ExtMethod
{
	private Method method; // A static method
	private Class[] parameterTypes; // Possible memory leak, but no worries, these are parameter types for the extension methods and will only have standard types.
	private boolean isVararg;

	public ExtMethod( Method method )
	{
		this.method = method;

		Class[] types = method.getParameterTypes();
		int count = types.length - 1;
		this.parameterTypes = new Class[ count ];
		System.arraycopy( types, 1, this.parameterTypes, 0, count );

		this.isVararg = ( method.getModifiers() & Modifier.TRANSIENT ) != 0;
	}

	public Class[] getParameterTypes()
	{
		return this.parameterTypes;
	}

	public boolean isVararg()
	{
		return this.isVararg;
	}

	public Method getMethod()
	{
		return this.method;
	}
}
