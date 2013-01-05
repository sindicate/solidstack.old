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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import solidstack.script.functions.Call;
import solidstack.script.functions.Def;
import solidstack.script.functions.Defined;
import solidstack.script.functions.Load;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Return;
import solidstack.script.functions.Throw;
import solidstack.script.functions.Val;
import funny.Symbol;




public class GlobalScope extends Scope
{
	static public final GlobalScope INSTANCE = new GlobalScope();

	public GlobalScope()
	{
		val( Symbol.apply( "call" ), new Call() );
		val( Symbol.apply( "def" ), new Def() ); // TODO Remove
		val( Symbol.apply( "defined" ), new Defined() );
		val( Symbol.apply( "load" ), new Load() );
		val( Symbol.apply( "print" ), new Print() );
		val( Symbol.apply( "println" ), new Println() );
		val( Symbol.apply( "return" ), new Return() ); // TODO Remove
		val( Symbol.apply( "scope" ), new solidstack.script.functions.Scope() ); // TODO Remove
		val( Symbol.apply( "throw" ), new Throw() ); // TODO Remove
		val( Symbol.apply( "val" ), new Val() ); // TODO Remove

		val( Symbol.apply( "boolean" ), boolean.class );
		val( Symbol.apply( "byte" ), byte.class );
		val( Symbol.apply( "char" ), char.class );
		val( Symbol.apply( "short" ), short.class );
		val( Symbol.apply( "int" ), int.class );
		val( Symbol.apply( "long" ), long.class );
		val( Symbol.apply( "float" ), float.class );
		val( Symbol.apply( "double" ), double.class );

		// java.lang

		val( Symbol.apply( "Boolean" ), Boolean.class );
		val( Symbol.apply( "Byte" ), Byte.class );
		val( Symbol.apply( "Character" ), Character.class );
		val( Symbol.apply( "CharSequence" ), CharSequence.class );
		val( Symbol.apply( "Class" ), Class.class );
		val( Symbol.apply( "Short" ), Short.class );
		val( Symbol.apply( "Integer" ), Integer.class );
		val( Symbol.apply( "Long" ), Long.class );
		val( Symbol.apply( "Float" ), Float.class );
		val( Symbol.apply( "Double" ), Double.class );
		val( Symbol.apply( "String" ), String.class );

		// java.math

		val( Symbol.apply( "BigInteger" ), BigInteger.class );
		val( Symbol.apply( "BigDecimal" ), BigDecimal.class );

		// java.util

		val( Symbol.apply( "ArrayList" ), ArrayList.class );
		val( Symbol.apply( "Arrays" ), Arrays.class );
		val( Symbol.apply( "Calendar" ), Calendar.class );
		val( Symbol.apply( "Date" ), Date.class );
		val( Symbol.apply( "LinkedList" ), LinkedList.class );
		val( Symbol.apply( "List" ), List.class );
		val( Symbol.apply( "Map" ), Map.class );
		val( Symbol.apply( "Set" ), Set.class );

		// java.reflect

		val( Symbol.apply( "Array" ), Array.class );

		// funny

		val( Symbol.apply( "Symbol" ), Symbol.class );
	}
}
