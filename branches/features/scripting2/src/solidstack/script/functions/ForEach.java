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

import solidstack.lang.Assert;
import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Null;

public class ForEach extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		Assert.isTrue( parameters.length == 2 );

		Object o = Script.deref( parameters[ 0 ] );
		Assert.isInstanceOf( o, Collection.class );

		Object f = Script.deref( parameters[ 1 ] );
		Assert.isInstanceOf( f, FunctionObject.class );

		FunctionObject fun = (FunctionObject)f;
		Collection collection = (Collection)o;

		Object result = Null.INSTANCE;
		for( Object object : collection )
			result = fun.call( thread, Arrays.asList( object ) );

		return result;
	}
}
