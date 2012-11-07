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

import solidstack.script.functions.Abs;
import solidstack.script.functions.Class;
import solidstack.script.functions.Def;
import solidstack.script.functions.Defined;
import solidstack.script.functions.ForEach;
import solidstack.script.functions.Length;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Substr;
import solidstack.script.functions.Throw;
import solidstack.script.functions.Upper;
import solidstack.script.functions.Val;
import solidstack.script.functions.StripMargin;




public class GlobalContext extends Context
{
	static public final GlobalContext INSTANCE = new GlobalContext();

	public GlobalContext()
	{
		val( "abs", new Abs() );
		val( "class", new Class() );
		val( "def", new Def() );
		val( "defined", new Defined() );
		val( "forEach", new ForEach() );
		val( "length", new Length() );
		val( "print", new Print() );
		val( "println", new Println() );
		val( "stripMargin", new StripMargin() );
		val( "substr", new Substr() );
		val( "throw", new Throw() );
		val( "upper", new Upper() );
		val( "val", new Val() );
	}
}
