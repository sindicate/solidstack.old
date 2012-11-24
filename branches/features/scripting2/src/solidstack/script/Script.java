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
import java.util.Collection;
import java.util.Map;

import solidstack.io.ReaderSourceReader;
import solidstack.io.SourceReader;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.FunnyString;
import solidstack.script.objects.Null;
import solidstack.script.objects.Util;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.Scope;

public class Script
{
	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	static public Script compile( String script )
	{
		return compile( new ReaderSourceReader( new StringReader( script ) ) );
	}

	static public Script compile( SourceReader reader )
	{
		return new Script( new ScriptParser( new ScriptTokenizer( reader ) ).parse() );
	}

	public Object execute( AbstractScope scope )
	{
		if( this.expression == null )
			return null;

		if( scope == null )
			scope = new Scope();

		ThreadContext thread = ThreadContext.init( scope );
		// TODO Catch the ScopeException and add the correct line number
		return Util.toJava( this.expression.evaluate( thread ) );
	}

	static public boolean isTrue( Object left )
	{
		if( left instanceof Ref )
		{
			if( ( (Ref)left ).isUndefined() )
				return false;
			left = ( (Ref)left ).get();
		}
		if( left == null )
			return false;
		if( left == Null.INSTANCE )
			return false;
		if( left instanceof Boolean )
			return (Boolean)left;
		if( left instanceof String )
			return ( (String)left ).length() != 0;
		if( left instanceof Collection )
			return !( (Collection<?>)left ).isEmpty();
		if( left instanceof Map )
			return !( (Map<?,?>)left ).isEmpty();
		if( left instanceof FunnyString )
			return !( (FunnyString)left ).isEmpty();
		return true;
	}
}
