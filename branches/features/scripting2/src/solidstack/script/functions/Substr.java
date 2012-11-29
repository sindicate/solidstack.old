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

import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Util;


// TODO Need to catch the java exceptions (index out of range)
public class Substr extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 2 && parameters.length != 3 )
			throw new ThrowException( "substr() needs 2 or 3 parameters", thread.cloneStack() );

		Object object = Util.deref( parameters[ 0 ] );
		if( !( object instanceof String ) )
			throw new ThrowException( "substr() needs a string as first parameter", thread.cloneStack() );

		Object start = Util.deref( parameters[ 1 ] );
		if( !( start instanceof Integer ) )
			throw new ThrowException( "substr() needs an integer as second parameter", thread.cloneStack() );

		if( parameters.length == 2 )
			return ( (String)object ).substring( (Integer)start );

		Object end = Util.deref( parameters[ 2 ] );
		if( !( end instanceof Integer ) )
			throw new ThrowException( "substr() needs an integer as third parameter", thread.cloneStack() );

		return ( (String)object ).substring( (Integer)start, (Integer)end );
	}
}
