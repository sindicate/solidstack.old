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

package solidstack.script.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodCall
{
	static public Class[] NO_PARAMETERS = new Class[ 0 ];

	public Object object;
	public Method method;
	public ExtensionMethod extMethod;
	public Constructor constructor;
	public Object[] args;
	public boolean isVarargCall;
	public Field field;

	public MethodCall( boolean isVarargCall )
	{
		this.isVarargCall = isVarargCall;
	}

	public MethodCall( boolean isVarargCall, Object... args )
	{
		this.args = args;
		this.isVarargCall = isVarargCall;
	}

	public Class[] getParameterTypes()
	{
		if( this.constructor != null )
			return this.constructor.getParameterTypes();
		if( this.extMethod != null )
			return this.extMethod.getParameterTypes();
		if( this.field != null )
			throw new UnsupportedOperationException();
		return this.method.getParameterTypes();
	}

	public Object invoke() throws InvocationTargetException
	{
		try
		{
			if( this.field != null )
			{
				if( !this.field.isAccessible() )
					this.field.setAccessible( true );
				return this.field.get( this.object );
			}
			if( this.args != null )
				this.args = Types.transformArguments( getParameterTypes(), this.args ); // TODO Why this.args?
			if( this.constructor != null )
				return this.constructor.newInstance( this.args );
			if( this.extMethod != null )
			{
				// Combine this with the array copying used for variable arity arguments.
				Object[] args = this.args;
				int count = args.length;
				Object[] newArgs = new Object[ count + 1 ];
				newArgs[ 0 ] = this.object;
				System.arraycopy( args, 0, newArgs, 1, count );
				return this.extMethod.getMethod().invoke( this.object, newArgs );
			}
			if( !this.method.isAccessible() )
				this.method.setAccessible( true );
			return this.method.invoke( this.object, this.args );
		}
		catch( InvocationTargetException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			Java.throwUnchecked( e );
		}
		return null;
	}

	public Member getMember()
	{
		if( this.constructor != null )
			return this.constructor;
		if( this.extMethod != null )
			return this.extMethod.getMethod();
		if( this.field != null )
			return this.field;
		return this.method;
	}

	public boolean isVararg()
	{
		if( this.constructor != null )
			return ( this.constructor.getModifiers() & Modifier.TRANSIENT ) != 0;
		if( this.extMethod != null )
			return this.extMethod.isVararg();
		if( this.field != null )
			return false;
		return ( this.method.getModifiers() & Modifier.TRANSIENT ) != 0;
	}

	public Object[] getArgs()
	{
		return this.args;
	}

	public void setArgs( Object[] value )
	{
		this.args = value;
	}
}
