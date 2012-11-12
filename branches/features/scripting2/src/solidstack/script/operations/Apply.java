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

package solidstack.script.operations;

import solidstack.script.Script;
import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.Operation;
import solidstack.script.java.Java;
import solidstack.script.objects.ClassAccess;
import solidstack.script.objects.FunctionInstance;
import solidstack.script.objects.Null;
import solidstack.script.objects.ObjectAccess;


public class Apply extends Operation
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = Script.single( this.left.evaluate( thread ) );
		if( left == Null.INSTANCE )
		{
			if( this.left instanceof Identifier )
				throw new ScriptException( "Function " + ( (Identifier)this.left ).getName() + " not found" );
			throw new ScriptException( "Cannot apply parameters to null" );
		}

		Object pars = null;
		if( this.right != null )
			pars = this.right.evaluate( thread );

		if( left instanceof FunctionInstance )
		{
			FunctionInstance f = (FunctionInstance)left;
			thread.pushStack( getLocation() );
			try
			{
				return f.call( thread, Script.toScriptParameters( pars ) );
			}
			finally
			{
				thread.popStack();
			}
		}

		if( left instanceof ObjectAccess )
		{
			ObjectAccess f = (ObjectAccess)left;
			thread.pushStack( getLocation() );
			try
			{
				return f.invoke( Script.toJavaParameters( pars ) );
			}
			finally
			{
				thread.popStack();
			}
		}

		if( left instanceof ClassAccess )
		{
			ClassAccess f = (ClassAccess)left;
			thread.pushStack( getLocation() );
			try
			{
				return f.invoke( Script.toJavaParameters( pars ) );
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
				return Java.construct( cls, Script.toJavaParameters( pars ) );
			}
			finally
			{
				thread.popStack();
			}
		}

		throw new ScriptException( "Cannot apply parameters to a " + left.getClass().getName() );
	}
}
