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

import solidstack.script.JavaException;
import solidstack.script.Returning;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.expressions.Expression;
import solidstack.script.java.Java;
import solidstack.script.objects.ClassMember;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.ObjectMember;
import solidstack.script.objects.Util;


public class Apply extends Operator
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left;
		if( this.left instanceof Member )
			left = ( (Member)this.left ).evaluateForApply( thread );
		else if( this.left instanceof StaticMember )
			left = ( (StaticMember)this.left ).evaluateForApply( thread );
		else
			left = this.left.evaluate( thread );
		left = Util.deref( left );

		if( left == null )
			throw new ThrowException( "Function is null", thread.cloneStack( getLocation() ) );

		Object[] pars = this.right != null ? Util.toArray( this.right.evaluate( thread ) ) : Util.EMPTY_ARRAY;

		if( left instanceof FunctionObject )
		{
			FunctionObject f = (FunctionObject)left;
			thread.pushStack( getLocation() );
			try
			{
				return f.call( thread, pars );
			}
			finally
			{
				thread.popStack();
			}
		}

		try
		{
			if( left instanceof ObjectMember )
			{
				ObjectMember f = (ObjectMember)left;
				thread.pushStack( getLocation() );
				try
				{
					return Java.invoke( f.getObject(), f.getName(), Util.toJavaParameters( pars, thread ) );
				}
				finally
				{
					thread.popStack();
				}
			}

			if( left instanceof ClassMember )
			{
				ClassMember f = (ClassMember)left;
				thread.pushStack( getLocation() );
				try
				{
					return Java.invokeStatic( f.getType(), f.getName(), Util.toJavaParameters( pars, thread ) );
				}
				finally
				{
					thread.popStack();
				}
			}

			if( left instanceof Class )
			{
				Class<?> cls = (Class<?>)left;
				thread.pushStack( getLocation() );
				try
				{
					return Java.construct( cls, Util.toJavaParameters( pars, thread ) );
				}
				finally
				{
					thread.popStack();
				}
			}
		}
		catch( InvocationTargetException e )
		{
			Throwable t = e.getCause();
			if( t instanceof Returning )
				throw (Returning)t;
			throw new JavaException( t, thread.cloneStack( getLocation() ) );
		}
		catch( Exception e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack( getLocation() ) );
//			throw new JavaException( e, thread.cloneStack( getLocation() ) );
		}

		throw new ThrowException( "Can't apply parameters to a " + left.getClass().getName(), thread.cloneStack( getLocation() ) );
	}
}
