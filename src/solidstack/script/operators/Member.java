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

package solidstack.script.operators;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.UndefinedPropertyException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.objects.Util;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.ScopeException;
import solidstack.script.scopes.UndefinedException;
import funny.Symbol;


public class Member extends Operator
{
	public Member( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		try
		{
			Object left = this.left.evaluate( thread );
			Assert.isInstanceOf( this.right, Identifier.class );
			Symbol right = ( (Identifier)this.right ).getSymbol();
			if( left == null )
				// TODO Use the Java exception hierarchy
				throw new ThrowException( "null reference: member: " + right.toString(), thread.cloneStack( getLocation() ) );
			if( left instanceof Scope ) // TODO This is part of the OO we want
			{
				Scope scope = (Scope)left;
				try
				{
					return scope.get( right );
				}
				catch( UndefinedException e )
				{
					throw new UndefinedPropertyException( right.toString(), thread.cloneStack( getLocation() ) );
				}
			}
			if( left instanceof Map )
				return ( (Map)left ).get( right.toString() );
			try
			{
				return Java.get( left, right.toString() );
			}
			catch( MissingFieldException e )
			{
				throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
			}
		}
		catch( ScopeException e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
		}
	}

	public Object assign( ThreadContext thread, Object value )
	{
		Object object = this.left.evaluate( thread );
		Symbol symbol = ( (Identifier)this.right ).getSymbol();

		if( object instanceof Map )
		{
			( (Map)object ).put( symbol.toString(), value );
			return value;
		}

		if( object instanceof Scope )
		{
			( (Scope)object ).set( symbol, value );
			return value;
		}

		try
		{
			if( object instanceof Type )
				Java.setStatic( ( (Type)object ).theClass(), symbol.toString(), value );
			else
				Java.set( object, symbol.toString(), value );
			return value;
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, thread.cloneStack( getLocation() ) );
		}
		catch( Returning e )
		{
			throw e;
	}
		catch( Exception e )
		{
			throw new ThrowException( e.getMessage() != null ? e.getMessage() : e.toString(), thread.cloneStack( getLocation() ) );
//			throw new JavaException( e, thread.cloneStack( getLocation() ) ); // TODO Debug flag or something?
}
	}

	public Object apply( ThreadContext thread, Object[] pars )
	{
		Object object = this.left.evaluate( thread );
		Symbol symbol = ( (Identifier)this.right ).getSymbol();

		if( object instanceof Scope ) // TODO And Map?
		{
			try
			{
				return ( (Scope)object ).apply( symbol, pars );
			}
			catch( UndefinedException e )
			{
				throw new UndefinedPropertyException( symbol.toString(), thread.cloneStack() );
			}
		}

		pars = Util.toJavaParameters( pars );
		try
		{
			if( object instanceof Type )
				return Java.invokeStatic( ( (Type)object ).theClass(), symbol.toString(), pars );
			return Java.invoke( object, symbol.toString(), pars );
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, thread.cloneStack( getLocation() ) );
		}
		catch( Returning e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new ThrowException( e.getMessage() != null ? e.getMessage() : e.toString(), thread.cloneStack( getLocation() ) );
//			throw new JavaException( e, thread.cloneStack( getLocation() ) ); // TODO Debug flag or something?
		}
	}

	public Object apply( ThreadContext thread, Map args )
	{
		throw new UnsupportedOperationException();
	}
}
