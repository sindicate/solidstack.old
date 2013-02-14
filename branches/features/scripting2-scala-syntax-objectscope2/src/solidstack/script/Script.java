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
import java.util.Map;

import solidstack.io.ReaderSourceReader;
import solidstack.io.SourceReader;
import solidstack.script.expressions.Expression;
import solidstack.script.java.Types;
import solidstack.script.objects.Tuple;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.MapScope;
import solidstack.script.scopes.ObjectScope;
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

	static private Object eval0( Expression expression, Object scope )
	{
		if( expression == null )
			return null;

		if( scope == null )
			scope = new DefaultScope();

		Scope s;
		if( scope instanceof Scope )
			s = (Scope)scope;
		else if( scope instanceof Map )
			s = new MapScope( (Map)scope );
		else
			s = new ObjectScope( scope );

		ThreadContext thread = ThreadContext.init( s );
		try
		{
			return expression.evaluate( thread );
		}
		catch( Returning e )
		{
			return e.getValue();
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

	static public Object eval( Expression expression, Object scope )
	{
		return Util.toJava( eval0( expression, scope ) );
	}

	static public boolean evalBoolean( Expression expression, Object scope )
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
		if( object instanceof Tuple )
		{
			// TODO Maybe we shouldn't even do this
			Tuple results = (Tuple)object;
			if( results.size() == 0 )
				return false;
			object = results.getLast();
		}
		return Types.castToBoolean( Util.toJava( object ) );
	}

	static public boolean isTrue( ThreadContext thread, Object object )
	{
		if( object instanceof Expression )
		{
			try
			{
				object = ((Expression)object).evaluate( thread );
			}
			catch( UndefinedPropertyException e ) // TODO But what if deeper into the expression this exception is thrown?
			{
				return false;
			}
		}
		return isTrue( object );
	}

	// --- Non static members

	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	public Object eval( Object scope )
	{
		return eval( this.expression, scope );
	}

	public Object eval()
	{
		return eval( this.expression, null );
	}

	public boolean evalBoolean( Object scope )
	{
		return evalBoolean( this.expression, scope );
	}

	public boolean evalBoolean()
	{
		return evalBoolean( this.expression, null );
	}

	// TODO WriteTo should actually be used to write the output of the script execution.
	public void writeTo( StringBuilder out )
	{
		if( this.expression != null )
			this.expression.writeTo( out );
	}
}
