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
import solidstack.script.objects.Util;
import funny.Symbol;


public class Apply extends Operator
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		if( this.left instanceof New )
		{
			Object left = ( (New)this.left ).evaluateForApply( thread );
			if( !( left instanceof Type ) )
				throw new ThrowException( "The new operator needs a type argument, not a " + left.getClass().getName(), thread.cloneStack( getLocation() ) );

			Object[] pars = this.right != null ? Util.toArray( this.right.evaluate( thread ) ) : Util.EMPTY_ARRAY;

			Class<?> cls = ( (Type)left ).theClass();
			try
			{
				thread.pushStack( getLocation() );
				try
				{
					return Java.construct( cls, Util.toJavaParameters( pars ) );
				}
				finally
				{
					thread.popStack();
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
			}
		}

		Expression left = this.left;

		// Build parameters

		List<Expression> vals;
		if( this.right instanceof BuildTuple )
			vals = ( (BuildTuple)this.right ).getExpressions();
		else if( this.right != null )
			vals = Arrays.asList( this.right );
		else
			vals = Collections.emptyList();

		if( !vals.isEmpty() && vals.get( 0 ) instanceof Assign )
		{
			Map<Symbol, Object> args = new HashMap<Symbol, Object>();
			for( Expression expression : vals )
			{
				if( !( expression instanceof Assign ) )
					throw new ThrowException( "All parameters must be named", thread.cloneStack( expression.getLocation() ) );
				Assign assign = (Assign)expression;
				if( !( assign.left instanceof Identifier ) )
					throw new ThrowException( "Parameter must be named with a variable identifier", thread.cloneStack( assign.left.getLocation() ) );
				args.put( ( (Identifier)assign.left ).getSymbol(), assign.right.evaluate( thread ) ); // TODO Error message
			}

			thread.pushStack( getLocation() );
			try
			{
				if( left instanceof Member )
					return ( (Member)left ).apply( thread, args );

				if( left instanceof Identifier )
					return ( (Identifier)left ).apply( thread, args );

				Object l = left.evaluate( thread );
				if( l instanceof FunctionObject )
					return ( (FunctionObject)l ).call( thread, args );

				throw new ThrowException( "Can't apply named parameters to a Java object", thread.cloneStack() );
			}
			finally
			{
				thread.popStack();
			}
		}

		for( Expression expression : vals )
			if( expression instanceof Assign )
				throw new ThrowException( "All parameters must be named", thread.cloneStack( expression.getLocation() ) );
		Object[] args = this.right != null ? Util.toArray( this.right.evaluate( thread ) ) : Util.EMPTY_ARRAY;

		thread.pushStack( getLocation() );
		try
		{
			if( left instanceof Member )
				return ( (Member)left ).apply( thread, args );

			if( left instanceof Identifier )
				return ( (Identifier)left ).apply( thread, args );

			Object l = left.evaluate( thread );
			if( l instanceof FunctionObject )
				return ( (FunctionObject)l ).call( thread, args );

			args = Util.toJavaParameters( args );
			try
			{
				return Java.invoke( l, "apply", args );
			}
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
//			throw new JavaException( e, thread.cloneStack( getLocation() ) );
		}
		}
		finally
		{
			thread.popStack();
		}
	}

		throw new ThrowException( "Can't apply parameters to a " + left.getClass().getName(), thread.cloneStack( getLocation() ) );
	}
}
