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
import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Type;
import solidstack.script.objects.Util;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.ScopeException;
import funny.Symbol;


public class Member extends Operator
{
	public Member( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		try
		{
			Object left = Util.deref( this.left.evaluate( thread ) );
			Assert.isInstanceOf( this.right, Identifier.class );
			Symbol right = ( (Identifier)this.right ).getSymbol();
			if( left == null )
				// TODO Use the Java exception hierarchy
				throw new ThrowException( "null reference: member: " + right.toString(), thread.cloneStack( getLocation() ) );
			if( left instanceof Scope ) // TODO This is part of the OO we want
				return ( (Scope)left ).get( right );
			if( left instanceof Map )
				return ( (Map)left ).get( right.toString() );
			try
			{
				if( left instanceof Type )
					return Java.getStatic( ( (Type)left ).theClass(), right.toString() );
				return Java.get( left, right.toString() );
			}
			catch( InvocationTargetException e )
			{
				Throwable t = e.getCause();
				if( t instanceof Returning )
					throw (Returning)t;
				throw new JavaException( t, thread.cloneStack( getLocation() ) );
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

//	@Override
//	public Object evaluateRef( ThreadContext thread )
//	{
//		try
//		{
//			Object left = Util.deref( this.left.evaluate( thread ) );
//			Assert.isInstanceOf( this.right, Identifier.class );
//			Symbol right = ( (Identifier)this.right ).getSymbol();
//			if( left == null )
//				throw new ThrowException( "null reference: member: " + right.toString(), thread.cloneStack( getLocation() ) );
//			if( left instanceof Scope ) // TODO This is part of the OO we want
//				return ( (Scope)left ).getRef( right );
//			// TODO Also read properties to look for Functions
//			return new ObjectMember( left, right );
//		}
//		catch( ScopeException e )
//		{
//			throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
//		}
//	}

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

//		ObjectMember ref = (ObjectMember)left;
//		Object object = ref.getObject();
//		String name = ref.getKey().toString();

		try
		{
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
		String name = ( (Identifier)this.right ).getSymbol().toString();

		if( object instanceof Scope ) // TODO And Map?
		{
			Object function = ( (Scope)object ).get( Symbol.apply( name ) );
			Assert.isInstanceOf( function, FunctionObject.class );
			return ( (FunctionObject)function ).call( thread, pars );
		}

//		ObjectMember ref = (ObjectMember)left;
//		Object object = ref.getObject();
//		String name = ref.getKey().toString();

		pars = Util.toJavaParameters( pars );
		thread.pushStack( getLocation() );
		try
		{
			if( object instanceof Type )
				return Java.invokeStatic( ( (Type)object ).theClass(), name, pars );
			return Java.invoke( object, name, pars );
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
		finally
		{
			thread.popStack();
		}
	}

	public Object apply( ThreadContext thread, Map args )
	{
		throw new UnsupportedOperationException();
	}
}
