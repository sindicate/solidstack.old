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

import java.util.Map;

import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Util;
import solidstack.script.scopes.MapScope;


// TODO Rename to toScope()?
public class Scope extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 1 )
			throw new ThrowException( "scope() needs exactly one parameter", thread.cloneStack() );
		Object object = Util.deref( parameters[ 0 ] );
		if( !( object instanceof Map ) )
			throw new ThrowException( "scope() needs a map parameter", thread.cloneStack() );
		return new MapScope( (Map<Object, Object>)object );
	}
}
