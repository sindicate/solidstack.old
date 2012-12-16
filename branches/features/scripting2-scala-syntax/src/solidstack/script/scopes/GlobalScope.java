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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import solidstack.script.functions.Abs;
import solidstack.script.functions.Call;
import solidstack.script.functions.Class;
import solidstack.script.functions.Def;
import solidstack.script.functions.Defined;
import solidstack.script.functions.Length;
import solidstack.script.functions.Load;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Return;
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
		val( Symbol.forString( "call" ), new Call() );
		val( Symbol.forString( "class" ), new Class() );
		val( Symbol.forString( "def" ), new Def() );
		val( Symbol.forString( "defined" ), new Defined() );
		val( Symbol.forString( "length" ), new Length() );
		val( Symbol.forString( "load" ), new Load() );
		val( Symbol.forString( "print" ), new Print() );
		val( Symbol.forString( "println" ), new Println() );
		val( Symbol.forString( "return" ), new Return() );
		val( Symbol.forString( "scope" ), new solidstack.script.functions.Scope() );
		val( Symbol.forString( "stripMargin" ), new StripMargin() );
		val( Symbol.forString( "substr" ), new Substr() );
		val( Symbol.forString( "throw" ), new Throw() );
		val( Symbol.forString( "upper" ), new Upper() );
		val( Symbol.forString( "val" ), new Val() );

		val( Symbol.forString( "boolean" ), boolean.class );
		val( Symbol.forString( "byte" ), byte.class );
		val( Symbol.forString( "char" ), char.class );
		val( Symbol.forString( "short" ), short.class );
		val( Symbol.forString( "int" ), int.class );
		val( Symbol.forString( "long" ), long.class );
		val( Symbol.forString( "float" ), float.class );
		val( Symbol.forString( "double" ), double.class );

		val( Symbol.forString( "Boolean" ), Boolean.class );
		val( Symbol.forString( "Byte" ), Byte.class );
		val( Symbol.forString( "Character" ), Character.class );
		val( Symbol.forString( "Short" ), Short.class );
		val( Symbol.forString( "Integer" ), Integer.class );
		val( Symbol.forString( "Long" ), Long.class );
		val( Symbol.forString( "Float" ), Float.class );
		val( Symbol.forString( "Double" ), Double.class );

		val( Symbol.forString( "BigInteger" ), BigInteger.class );
		val( Symbol.forString( "BigDecimal" ), BigDecimal.class );
		val( Symbol.forString( "String" ), String.class );

		val( Symbol.forString( "List" ), List.class );
		val( Symbol.forString( "Set" ), List.class );
		val( Symbol.forString( "Map" ), List.class );
	}
}
