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

import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.FunctionInstance;
import solidstack.script.Operation;
import solidstack.script.ThreadContext;

public class stripMargin extends FunctionInstance
{
	@Override
	public Object call( List<Object> parameters, ThreadContext thread )
	{
		Assert.isTrue( parameters.size() == 1 );
		Operation.unwrapList( parameters );

		Object object = parameters.get( 0 );
		Assert.isInstanceOf( object, String.class );
		return ( (String)object ).replaceAll( "(?m)^[ \\t]*\\|", "" );
	}
}
