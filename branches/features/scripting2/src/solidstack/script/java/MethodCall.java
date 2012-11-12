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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodCall implements Cloneable
{
	static public Class[] NO_PARAMETERS = new Class[ 0 ];

	public Object object;
	public Method method;
	public Constructor constructor;
	private Object[] args;
	public boolean isVarargCall;
	public int difficulty;

	public MethodCall( boolean isVarargCall, int difficulty, Object... args )
	{
		this.args = args;
		this.isVarargCall = isVarargCall;
		this.difficulty = difficulty;
	}

	public Class[] getParameterTypes()
	{
		if( this.constructor != null )
			return this.constructor.getParameterTypes();
		return this.method.getParameterTypes();
	}

	public Class getDeclaringClass()
	{
		return this.method.getDeclaringClass();
	}

	public Object invoke()
	{
		this.args = Resolver.transformArguments( getParameterTypes(), this.args );
		try
		{
			if( this.constructor != null )
				return this.constructor.newInstance( this.args );
			if( !this.method.isAccessible() )
				this.method.setAccessible( true );
			return this.method.invoke( this.object, this.args );
		}
		catch( InvocationTargetException e )
		{
			Java.throwUnchecked( e.getCause() );
		}
		catch( Exception e )
		{
			Java.throwUnchecked( e );
		}
		return null;
	}

	public String getName()
	{
		return this.method.getName();
	}

	public boolean isVararg()
	{
		return ( this.method.getModifiers() & Modifier.TRANSIENT ) != 0;
	}

	public Object getReturnType()
	{
		return this.method.getReturnType();
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
