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

import org.testng.annotations.Test

public class ScriptTests2
{
	@Test
	static public void test1()
	{
		// TODO Without the ) you get no error from the parser
		ScriptTests.test( '''
			println( "Multiline strings
work too" );
			println( "Single line \\
with escaped newline" );
			i = 0;
			while( i < 10;
				println( i );
				i++
			);
			''',
			new BigDecimal( 9 )
		);
	}

	@Test
	static public void test2()
	{
		ScriptTests.test( '''
			i = 0;
			f = fun( ; println( i ); ++i );
			while( i < 10; f() );
			println( "total: " + while( i < 20; f() ) + " numbers" );
			''',
			"total: 20 numbers"
		);
		// FIXME String addition with nulls gives a NPE
		// FIXME The string with the while loop does not work when it is a superstring
	}

//	@Test
//	static public void test3()
//	{
//		ScriptTests.test( '''
//			// This syntax runs to the end of the container
//			foo = ( f -> f( "test" ) );
//			foo( a -> a );
//			foo( a, b -> a ); // This has 2 parameters: a and a closure
//			foo( a -> a, b ); // This has 1 parameter: a closure returning a tuple
//			''',
//			"test"
//		);
//	}
}
