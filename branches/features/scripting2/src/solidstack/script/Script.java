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

public class Script
{
	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	// TODO Add location
	static public Script compile( String script )
	{
		ScriptTokenizer t = new ScriptTokenizer( new ReaderSourceReader( new StringReader( script ) ) );
		ScriptParser p = new ScriptParser( t );
		Expression result = p.parse();
		return new Script( result );
	}

	public Object execute( Context context )
	{
		if( this.expression == null )
			return null;

		if( context == null )
			context = new Context();

		ThreadContext thread = ThreadContext.init( context );

		Object result = Operation.evaluateAndUnwrap( this.expression, thread );
		if( result == Null.INSTANCE )
			return null;
		if( result instanceof SuperString )
			return result.toString();
		if( result instanceof ObjectAccess )
			return ( (ObjectAccess)result ).get();
		return result;
	}
}
