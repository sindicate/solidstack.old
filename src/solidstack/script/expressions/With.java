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

package solidstack.script.expressions;

import java.util.Map;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.objects.Util;
import solidstack.script.scopes.CombinedScope;
import solidstack.script.scopes.MapScope;
import solidstack.script.scopes.ObjectScope;
import solidstack.script.scopes.Scope;



public class With extends LocalizedExpression
{
	private Expression object;
	private Expression expression;

	public With( SourceLocation location, Expression object, Expression expression )
	{
		super( location );
		this.object = object;
		this.expression = expression;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object object = Util.deref( this.object.evaluate( thread ) );
		Scope scope;
		if( object instanceof Scope )
			scope = (Scope)object;
		else if( object instanceof Map )
			scope = new MapScope( (Map)object );
		else
			scope = new ObjectScope( object );
		scope = new CombinedScope( scope, thread.getScope() );
		scope = thread.swapScope( scope );
		try
		{
			return this.expression.evaluate( thread );
		}
		finally
		{
			thread.swapScope( scope );
		}
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "with(" );
		this.object.writeTo( out );
		out.append( ')' );
		this.expression.writeTo( out );
	}
}
