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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import solidstack.script.ScriptException;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.Operation;
import solidstack.script.java.Java;
import solidstack.script.objects.ClassAccess;
import solidstack.script.objects.FunctionInstance;
import solidstack.script.objects.ObjectAccess;
import solidstack.script.objects.TupleValue;


public class Apply extends Operation
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = evaluateAndUnwrap( this.left, thread );
		if( left == null )
		{
			if( this.left instanceof Identifier )
				throw new ScriptException( "Function " + ( (Identifier)this.left ).getName() + " not found" );
			throw new ScriptException( "Cannot apply parameters to null" );
		}

		if( left instanceof FunctionInstance )
		{
			FunctionInstance f = (FunctionInstance)left;
			Object pars = this.right.evaluate( thread );

			List<Object> list;
			if( pars instanceof TupleValue )
				list = ( (TupleValue)pars ).getValues();
			else if( pars != null )
				list = Arrays.asList( pars );
			else
				list = Collections.emptyList(); // TODO Can be a constant maybe

			thread.pushStack( getLocation() );
			Object result = f.call( list, thread );
			thread.popStack();

			return result;
		}

		if( left instanceof ObjectAccess )
		{
			ObjectAccess f = (ObjectAccess)left;
			Object pars = this.right.evaluate( thread );
			if( pars instanceof TupleValue )
				return f.invoke( unwrapList( ( (TupleValue)pars ).getValues() ).toArray() ); // TODO unwrap array
			if( pars != null )
				return f.invoke( unwrap( pars ) );
			return f.invoke();
		}

		if( left instanceof ClassAccess )
		{
			ClassAccess f = (ClassAccess)left;
			Object pars = this.right.evaluate( thread );
			if( pars instanceof TupleValue )
				return f.invoke( unwrapList( ( (TupleValue)pars ).getValues() ).toArray() ); // TODO unwrap array
			if( pars != null )
				return f.invoke( unwrap( pars ) );
			return f.invoke();
		}

		if( left instanceof Class )
		{
			Class<?> cls = (Class<?>)left;
			Object pars = this.right.evaluate( thread );
			if( pars instanceof TupleValue )
			{
				List<Object> list = ( (TupleValue)pars ).getValues();
				return Java.construct( cls, list.toArray() );
			}
			if( pars != null )
				return Java.construct( cls, new Object[] { pars } );
			return Java.construct( cls );
		}

		throw new ScriptException( "Cannot apply parameters to a " + left.getClass().getName() );
	}
}
