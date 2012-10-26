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

package solidstack.script;

import java.util.List;

public class SuperString
{
	private List<Expression> expressions;
	private Context context;

	public SuperString( List<Expression> expressions, Context context )
	{
		this.expressions = expressions;
		this.context = context; // FIXME Possibly need to clone the whole context hierarchy (flattened).
	}

	@Override
	public String toString()
	{
		Context context = new Context( this.context ); // Subcontext only stores new variables and local (deffed) variables.

		StringBuilder result = new StringBuilder();
		for( Expression e : this.expressions )
		{
			Object object = Operation.evaluateAndUnwrap( e, context );
			result.append( object );
		}
		return result.toString();
	}

	public boolean isEmpty()
	{
		Context context = new Context( this.context ); // Subcontext only stores new variables and local (deffed) variables.

		for( Expression e : this.expressions )
		{
			Object object = Operation.evaluateAndUnwrap( e, context );
			if( object.toString().length() != 0 )
				return false;
		}
		return true;
	}
}
