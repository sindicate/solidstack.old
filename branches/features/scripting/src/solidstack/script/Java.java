package solidstack.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import solidstack.lang.SystemException;


public class Java
{
	static private Method getMethod( Object object, String name, Object[] args ) throws NoSuchMethodException
	{
		int pars = args.length;
		Method[] methods = object.getClass().getMethods();
		for( Method method : methods )
			if( method.getName().equals( name ) )
				if( method.getParameterTypes().length == pars ) // TODO Vararg
					return method;

		throw new NoSuchMethodException( object.getClass().getName() + "." + name + "()" );
	}

	static public Object invoke( Object object, String name, Object... args )
	{
		Method method;
		try
		{
			method = getMethod( object, name, args );
		}
		catch( NoSuchMethodException e )
		{
			throw new SystemException( e );
		}
		convert( args, method.getParameterTypes() );
		try
		{
			return method.invoke( object, args );
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

	private static void convert( Object[] args, Class<?>[] types )
	{
		int count = args.length;
		for( int i = 0; i < count; i++ )
		{
			Object arg = args[ i ];
			if( arg == null )
				continue;
			Class<?> type = types[ i ];
			if( arg instanceof BigDecimal )
			{
				if( type == int.class )
				{
					args[ i ] = ( (BigDecimal)arg ).intValue();
				}
			}
		}
	}
}
