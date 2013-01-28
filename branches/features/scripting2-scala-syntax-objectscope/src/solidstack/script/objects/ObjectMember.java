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

package solidstack.script.objects;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.ScopeException;
import funny.Symbol;



public class ObjectMember implements Ref
{
	private Object object;
	private Symbol key;

	public ObjectMember( Object object, Symbol key )
	{
		this.object = object;
		this.key = key;
	}

	public Object getObject()
	{
		return this.object;
	}

	public Symbol getKey()
	{
		return this.key;
	}

	public boolean isUndefined()
	{
		throw new UnsupportedOperationException();
	}

	public Object get()
	{
		try
		{
			return Java.get( this.object, this.key.toString() ); // TODO Use resolve() instead.
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
			if( this.object instanceof Map )
				( (Map)this.object ).put( this.key.toString(), value );
			else
				Java.set( this.object, this.key.toString(), value ); // TODO Use resolve() instead.
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
}
