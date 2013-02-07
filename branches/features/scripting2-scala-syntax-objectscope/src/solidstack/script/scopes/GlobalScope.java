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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import solidstack.script.functions.Call;
import solidstack.script.functions.ClassOf;
import solidstack.script.functions.Defined;
import solidstack.script.functions.Load;
import solidstack.script.functions.LoadClass;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Return;
import solidstack.script.functions.Throw;
import solidstack.script.objects.Type;
import funny.Symbol;




public class GlobalScope extends Scope
{
	static public final GlobalScope INSTANCE = new GlobalScope();

	public GlobalScope()
	{
		val( Symbol.apply( "call" ), new Call() );
		val( Symbol.apply( "classOf" ), new ClassOf() );
		val( Symbol.apply( "defined" ), new Defined() );
		val( Symbol.apply( "load" ), new Load() );
		val( Symbol.apply( "loadClass" ), new LoadClass() ); // TODO Or loadType?
		val( Symbol.apply( "print" ), new Print() );
		val( Symbol.apply( "println" ), new Println() );
		val( Symbol.apply( "return" ), new Return() ); // TODO Remove
		val( Symbol.apply( "scope" ), new solidstack.script.functions.Scope() ); // TODO Remove
		val( Symbol.apply( "throw" ), new Throw() ); // TODO Remove

		// Primitives

		val( Symbol.apply( "boolean" ), new Type( boolean.class ) );
		val( Symbol.apply( "byte" ), new Type( byte.class ) );
		val( Symbol.apply( "char" ), new Type( char.class ) );
		val( Symbol.apply( "short" ), new Type( short.class ) );
		val( Symbol.apply( "int" ), new Type( int.class ) );
		val( Symbol.apply( "long" ), new Type( long.class ) );
		val( Symbol.apply( "float" ), new Type( float.class ) );
		val( Symbol.apply( "double" ), new Type( double.class ) );

		// java.lang

		// TODO Use script eval or default methods
		val( Symbol.apply( "Boolean" ), new Type( Boolean.class ) );
		val( Symbol.apply( "Byte" ), new Type( Byte.class ) );
		val( Symbol.apply( "Character" ), new Type( Character.class ) );
		val( Symbol.apply( "CharSequence" ), new Type( CharSequence.class ) );
		val( Symbol.apply( "Class" ), new Type( Class.class ) );
		val( Symbol.apply( "Short" ), new Type( Short.class ) );
		val( Symbol.apply( "Integer" ), new Type( Integer.class ) );
		val( Symbol.apply( "Long" ), new Type( Long.class ) );
		val( Symbol.apply( "Float" ), new Type( Float.class ) );
		val( Symbol.apply( "Double" ), new Type( Double.class ) );
		val( Symbol.apply( "String" ), new Type( String.class ) );

		// java.math

		val( Symbol.apply( "BigInteger" ), new Type( BigInteger.class ) );
		val( Symbol.apply( "BigDecimal" ), new Type( BigDecimal.class ) );

		// java.util

		val( Symbol.apply( "ArrayList" ), new Type( ArrayList.class ) );
		val( Symbol.apply( "Arrays" ), new Type( Arrays.class ) );
		val( Symbol.apply( "Calendar" ), new Type( Calendar.class ) );
		val( Symbol.apply( "Date" ), new Type( Date.class ) );
		val( Symbol.apply( "LinkedHashMap" ), new Type( LinkedHashMap.class ) );
		val( Symbol.apply( "LinkedHashSet" ), new Type( LinkedHashSet.class ) );
		val( Symbol.apply( "LinkedList" ), new Type( LinkedList.class ) );
		val( Symbol.apply( "List" ), new Type( List.class ) );
		val( Symbol.apply( "Map" ), new Type( Map.class ) );
		val( Symbol.apply( "Properties" ), new Type( Properties.class ) );
		val( Symbol.apply( "Set" ), new Type( Set.class ) );

		// java.reflect

		val( Symbol.apply( "Array" ), new Type( Array.class ) );

		// funny

		val( Symbol.apply( "Symbol" ), new Type( Symbol.class ) );
	}
}
