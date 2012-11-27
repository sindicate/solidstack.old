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

import java.io.StringReader;

import solidstack.io.ReaderSourceReader;
import solidstack.io.SourceReader;
import solidstack.script.expressions.Expression;
import solidstack.script.java.Types;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.Scope;

public class Script
{
	static public Script compile( String script )
	{
		return compile( new ReaderSourceReader( new StringReader( script ) ) );
	}

	static public Script compile( SourceReader reader )
	{
		return new Script( new ScriptParser( new ScriptTokenizer( reader ) ).parse() );
	}

	static private Object eval0( Expression expression, AbstractScope scope )
	{
		if( expression == null )
			return null;

		if( scope == null )
			scope = new Scope();

		ThreadContext thread = ThreadContext.init( scope );
		try
		{
			return expression.evaluate( thread );
		}
		catch( ThrowException e )
		{
			throw new ScriptException( e );
		}
//		catch( JavaException e )
//		{
//			throw new ScriptException( e );
//		}
	}

	static public Object eval( Expression expression, AbstractScope scope )
	{
		return Util.toJava( eval0( expression, scope ) );
	}

	static public boolean evalBoolean( Expression expression, AbstractScope scope )
	{
		return isTrue( eval0( expression, scope ) );
	}

	static public boolean isTrue( Object object )
	{
		if( object instanceof Ref )
		{
			if( ( (Ref)object ).isUndefined() )
				return false;
			object = ( (Ref)object ).get();
		}
		return Types.castToBoolean( Util.toJava( object ) );
	}

	// --- Non static members

	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	public Object execute( AbstractScope scope )
	{
		return eval( this.expression, scope );
	}

	public boolean evalBoolean( AbstractScope scope )
	{
		return evalBoolean( this.expression, scope );
	}
}
