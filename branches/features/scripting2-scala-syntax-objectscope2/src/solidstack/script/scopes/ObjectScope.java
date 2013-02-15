package solidstack.script.scopes;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.objects.Util;
import funny.Symbol;

public class ObjectScope extends AbstractScope
{
	private Object object;

	public ObjectScope( Object object )
	{
		this.object = object;
	}

	@Override
	public Variable var( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get( Symbol symbol )
	{
		try
		{
			return Java.get( this.object, symbol.toString() ); // TODO Use resolve() instead.
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, ThreadContext.get().cloneStack( /* TODO getLocation() */ ) );
		}
		catch( MissingFieldException e )
		{
			throw new UndefinedException();
		}
	}

	@Override
	protected void set0( Symbol symbol, Object value )
	{
		try
		{
			Java.set( this.object, symbol.toString(), value ); // TODO Use resolve() instead.
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, ThreadContext.get().cloneStack( /* TODO getLocation() */ ) );
		}
		catch( MissingFieldException e )
		{
			throw new UndefinedException();
		}
	}

	public Object apply( Symbol symbol, Object... pars )
	{
		pars = Util.toJavaParameters( pars );
		try
		{
			return Java.invoke( this.object, symbol.toString(), pars );
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, ThreadContext.get().cloneStack() );
		}
		catch( Returning e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new ThrowException( e.getMessage() != null ? e.getMessage() : e.toString(), ThreadContext.get().cloneStack() );
//			throw new JavaException( e, thread.cloneStack( getLocation() ) ); // TODO Debug flag or something?
		}
	}

	public Object apply( Symbol symbol, Map args )
	{
		throw new UnsupportedOperationException();
	}
}
