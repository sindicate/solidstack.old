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

package solidstack.script.functions;

import funny.Symbol;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.AbstractScope.Value;

public class Val extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 1 ) // TODO Maybe this could be more than one
			throw new ThrowException( "val() needs exactly one parameter", thread.cloneStack() );
		Object object = parameters[ 0 ];
		if( !( object instanceof Ref ) )
			throw new ThrowException( "val() needs a variable identifier as parameter", thread.cloneStack() );
		Symbol symbol = ( (Value)object ).getKey();
		return thread.getScope().val( symbol, null );
	}
}
