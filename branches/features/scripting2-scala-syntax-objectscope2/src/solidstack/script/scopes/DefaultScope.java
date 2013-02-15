/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.script.scopes;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.java.Java;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Type;
import solidstack.script.objects.Util;
import funny.Symbol;




public class DefaultScope extends AbstractScope
{
	static public final Symbol THIS = Symbol.apply( "this" );

	protected Scope parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public DefaultScope()
	{
		var( THIS, this );
	}

	public DefaultScope( Scope parent )
	{
		this();
		this.parent = parent;
	}

	// For testing
	public void clear()
	{
		this.values.clear();
	}

	@Override
	public Variable var( Symbol symbol, Object value )
	{
		Variable result = new Variable( symbol, value );
		this.values.put( result );
		return result;
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		Value result = new Value( symbol, value );
		this.values.put( result );
		return result;
	}

	@Override
	public Object get( Symbol symbol )
	{
		Value ref = this.values.get( symbol );
		if( ref != null )
			return ref.get();
		if( this.parent != null )
			return this.parent.get( symbol );
		throw new UndefinedException();
	}

	@Override
	protected void set0( Symbol symbol, Object value )
	{
		Value ref = this.values.get( symbol );
		if( ref == null )
			throw new UndefinedException();
		if( ref instanceof Variable )
			( (Variable)ref ).set( value );
		else
			throw new ReadOnlyException();
	}

	public Object apply( Symbol symbol, Object... args )
	{
		Value ref = this.values.get( symbol );
		if( ref != null )
		{
			Object object = ref.get();
			if( object == null )
				throw new ThrowException( "Function is null", ThreadContext.get().cloneStack() );

			if( object instanceof FunctionObject )
				return ( (FunctionObject)object ).call( ThreadContext.get(), args );

			Object[] pars = Util.toJavaParameters( args );
			try
			{
				if( object instanceof Type )
					return Java.invokeStatic( ( (Type)object ).theClass(), "apply", pars );
				return Java.invoke( object, "apply", pars );
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
//				throw new JavaException( e, thread.cloneStack( getLocation() ) ); // TODO Debug flag or something?
			}
		}
		if( this.parent != null )
			return this.parent.apply( symbol, args );
		throw new UndefinedException();
	}

	public Object apply( Symbol symbol, Map args )
	{
		Value ref = this.values.get( symbol );
		if( ref != null )
		{
			Object object = ref.get();
			if( object == null )
				throw new ThrowException( "Function is null", ThreadContext.get().cloneStack() );

			if( object instanceof FunctionObject )
				return ( (FunctionObject)object ).call( ThreadContext.get(), args );

			throw new UnsupportedOperationException();
		}
		if( this.parent != null )
			return this.parent.apply( symbol, args );
		throw new UndefinedException();
	}
}
