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



import org.springframework.util.Assert;

import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.Identifier;
import solidstack.script.ObjectAccess;
import solidstack.script.Operation;
import solidstack.script.ThreadContext;


public class Access extends Operation
{
	public Access( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	public Object evaluate( ThreadContext thread )
	{
		Object left = evaluateAndUnwrap( this.left, thread );
		Assert.isInstanceOf( Identifier.class, this.right );
		String right = ( (Identifier)this.right ).getName();
		if( left instanceof Context )
			return ( (Context)left ).get( right );
		return new ObjectAccess( left, right );
	}
}
