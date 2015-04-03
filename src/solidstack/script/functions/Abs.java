/*--
 * Copyright 2012 Ren� M. de Bloois
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
import solidstack.script.operators.Operator;

public class Abs extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 1 )
			throw new ThrowException( "abs() needs exactly one parameter", thread.cloneStack() );
		return Operator.abs( Util.deref( parameters[ 0 ] ) );
	}
}
