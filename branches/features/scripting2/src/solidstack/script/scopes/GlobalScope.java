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

package solidstack.script.scopes;

import solidstack.script.functions.Abs;
import solidstack.script.functions.Class;
import solidstack.script.functions.Def;
import solidstack.script.functions.Defined;
import solidstack.script.functions.ForEach;
import solidstack.script.functions.Length;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.StripMargin;
import solidstack.script.functions.Substr;
import solidstack.script.functions.Throw;
import solidstack.script.functions.Upper;
import solidstack.script.functions.Val;




public class GlobalScope extends Scope
{
	static public final GlobalScope INSTANCE = new GlobalScope();

	public GlobalScope()
	{
		val( Symbol.forString( "abs" ), new Abs() );
		val( Symbol.forString( "class" ), new Class() );
		val( Symbol.forString( "def" ), new Def() );
		val( Symbol.forString( "defined" ), new Defined() );
		val( Symbol.forString( "forEach" ), new ForEach() );
		val( Symbol.forString( "length" ), new Length() );
		val( Symbol.forString( "print" ), new Print() );
		val( Symbol.forString( "println" ), new Println() );
		val( Symbol.forString( "stripMargin" ), new StripMargin() );
		val( Symbol.forString( "substr" ), new Substr() );
		val( Symbol.forString( "throw" ), new Throw() );
		val( Symbol.forString( "upper" ), new Upper() );
		val( Symbol.forString( "val" ), new Val() );
	}
}
