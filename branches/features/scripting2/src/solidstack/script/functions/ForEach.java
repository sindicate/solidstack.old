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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.ThreadContext;
import solidstack.script.expressions.Operation;
import solidstack.script.objects.FunctionInstance;

public class ForEach extends FunctionInstance
{
	@Override
	public Object call( List<Object> parameters, ThreadContext thread )
	{
		Assert.isTrue( parameters.size() == 2 );
		Operation.unwrapList( parameters );

		Object o = parameters.get( 0 );
		Assert.isInstanceOf( o, Collection.class );

		Object f = parameters.get( 1 );
		Assert.isInstanceOf( f, FunctionInstance.class );

		FunctionInstance fun = (FunctionInstance)f;
		Collection collection = (Collection)o;

		Object result = null;
		for( Object object : collection )
			result = fun.call( Arrays.asList( object ), thread );

		return result;
	}
}
