package solidstack.script.scopes;

import java.lang.reflect.InvocationTargetException;

import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import funny.Symbol;

public class ObjectScope extends AbstractScope
{
	private Object object;

	public ObjectScope( Object object )
	{
		this.object = object;
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

//	@Override
//	public Ref findRef( Symbol symbol )
//	{
//		return new ObjectRef( symbol.toString() );
//	}

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

	public class ObjectRef implements Ref
	{
		private String key; // TODO Not Symbol?

		public ObjectRef( String key )
		{
			this.key = key;
		}

		public Object getObject()
		{
			return ObjectScope.this.object;
		}

		public Symbol getKey()
		{
			return Symbol.apply( this.key );
		}

		public Object get()
		{
			try
			{
				return Java.get( ObjectScope.this.object, this.key ); // TODO Use resolve() instead.
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
				throw new ScopeException( "'" + this.key + "' undefined" );
			}
		}

		public void set( Object value )
		{
			try
			{
				Java.set( ObjectScope.this.object, this.key, value ); // TODO Use resolve() instead.
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
				throw new ScopeException( "'" + this.key + "' undefined" );
			}
		}

		public boolean isUndefined()
		{
			throw new UnsupportedOperationException(); // TODO
		}
	}
}
