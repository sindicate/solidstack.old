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

import java.util.ArrayList;
import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.FunctionInstance;
import solidstack.script.Identifier;
import solidstack.script.Operation;
import solidstack.script.Tuple;


public class Lambda extends Operation
{
	public Lambda( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Assert.isInstanceOf( this.left, Tuple.class );
		List<String> parameters = new ArrayList<String>();
		for( Expression expression : ( (Tuple)this.left ).getExpressions() )
		{
			Assert.isInstanceOf( expression, Identifier.class );
			parameters.add( ( (Identifier)expression ).getName() );
		}
		return new FunctionInstance( parameters, this.right, context );
	}
}
