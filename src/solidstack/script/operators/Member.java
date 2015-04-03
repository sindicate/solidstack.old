/*--
 * Copyright 2012 Ren� M. de Bloois
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

import solidstack.lang.Assert;
import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.objects.ObjectMember;
import solidstack.script.objects.Type;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope;
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
			Assert.isFalse( left == null, "member: " + right.toString() );
			if( left instanceof AbstractScope ) // TODO This is part of the OO we want
				return ( (AbstractScope)left ).getRef( right );
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

	public Object evaluateForApply( ThreadContext thread )
	{
		try
		{
			Object left = Util.deref( this.left.evaluate( thread ) );
			Assert.isInstanceOf( this.right, Identifier.class );
			Symbol right = ( (Identifier)this.right ).getSymbol();
			Assert.isFalse( left == null, "member: " + right.toString() );
			if( left instanceof AbstractScope ) // TODO This is part of the OO we want
				return ( (AbstractScope)left ).getRef( right );
			// TODO Also read properties to look for Functions
			return new ObjectMember( left, right.toString() );
		}
		catch( ScopeException e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
		}
	}
}
