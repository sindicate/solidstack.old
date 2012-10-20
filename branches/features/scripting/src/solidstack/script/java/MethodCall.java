package solidstack.script.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import solidstack.script.ScriptException;

public class MethodCall implements Cloneable
{
	static public Class[] NO_PARAMETERS = new Class[ 0 ];

	public Object object;
	public Method method;
	private Object[] args;
	public boolean isVarargCall;
	public int difficulty;

	public MethodCall( boolean isVarargCall, int difficulty, Object... args )
	{
		this.args = args;
		this.isVarargCall = isVarargCall;
		this.difficulty = difficulty;
	}

	public Class[] getParameterTypes()
	{
		return this.method.getParameterTypes();
	}

	public Class getDeclaringClass()
	{
		return this.method.getDeclaringClass();
	}

	public Object invoke()
	{
		this.args = Resolver.transformArguments( this.method.getParameterTypes(), this.args );
		try
		{
			return this.method.invoke( this.object, this.args );
		}
		catch( IllegalAccessException e )
		{
			throw new ScriptException( e );
		}
		catch( InvocationTargetException e )
		{
			throw new ScriptException( e.getCause() );
		}
	}

	public String getName()
	{
		return this.method.getName();
	}

	public boolean isVararg()
	{
		return ( this.method.getModifiers() & Modifier.TRANSIENT ) != 0;
	}

	public Object getReturnType()
	{
		return this.method.getReturnType();
	}

	public Object[] getArgs()
	{
		return this.args;
	}

	public void setArgs( Object[] value )
	{
		this.args = value;
	}
}
