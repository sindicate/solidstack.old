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

import java.util.List;
import java.util.ListIterator;

import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.FunctionInstance;
import solidstack.script.Identifier;
import solidstack.script.ObjectAccess;
import solidstack.script.Operation;
import solidstack.script.ScriptException;
import solidstack.script.Tuple;
import solidstack.script.Value;

public class Apply extends Operation
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		if( left == null )
		{
			if( this.left instanceof Identifier )
				throw new ScriptException( "Function " + ( (Identifier)this.left ).getName() + " not found" );
			throw new ScriptException( "Cannot apply parameters to null" );
		}

		if( left instanceof FunctionInstance )
		{
			FunctionInstance f = (FunctionInstance)left;
			List<Object> pars = ( (Tuple)this.right ).evaluateSeparate( context );
			unwrap( pars );
			return f.call( pars );
		}

		if( left instanceof ObjectAccess )
		{
			ObjectAccess f = (ObjectAccess)left;
			List<Object> pars = ( (Tuple)this.right ).evaluateSeparate( context ); // TODO Unwrap needed here?
			return f.invoke( pars.toArray() );
		}

		throw new ScriptException( "Cannot apply parameters to a " + left.getClass().getName() );
	}

	private void unwrap( List<Object> objects )
	{
		ListIterator i = objects.listIterator();
		while( i.hasNext() )
		{
			Object o = i.next();
			if( o instanceof Value )
				i.set( ( (Value)o ).get() );
		}
	}
}
