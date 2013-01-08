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


public class ScriptTests2 extends Util
{
	@Test
	static public void test1()
	{
		// TODO Without the ) you get no error from the parser
		// TODO Find a way to parse a string with | as the left border marker.
		test( '''
			println( "\\
				Multiline strings
				work too" );
			println( "Single line \\
with escaped newline" );
			println( "\\
|Multiline |strings
				|with indentation stripped".stripMargin() );
			i = 0;
			while( i < 10 ) ( // This is a comment
				println( i );
				i = i + 1
			);
			// Comment at the end''',
			10
		);
	}

	@Test
	static public void test2()
	{
		test( '''
			i = 0;
			f = () => ( println( i ); i = i + 1 );
			while( i < 10 ) f();
			println( "total: " + ( while( i < 20 ) f() ) + " numbers" );
			''',
			"total: 20 numbers"
		);
		// FIXME String addition with nulls gives a NPE
		// FIXME The string with the while loop does not work when it is a superstring
	}

	@Test
	static public void test3()
	{
		eval( '''
			o = {
				a = 4;
				f = b => b * a;
				this;
			};
			if( ( got = o.f( 3 ) ) != 12 )
				throw( "Expected 12, got ${got}" );
			oo = {
				f = b => b;
				this + o;
			};
			if( ( got = oo.f( 4 ) ) != 4 )
				throw( "Expected 4, got ${got}" );
			'''
		);
		eval( '''
			// Creates a function. The function returns its own scope.
			c = a => {
				f = b => b * a;
				this;
			};
			// Calls the function and receives the new scope. The scope contains f and a = 5.
			o = c( 5 );
			// Calls f in the scope o and returns 15 because a = 5.
			if( ( got = o.f( 3 ) ) != 15 )
				throw( "Expected 15, got ${got}" );
			// Creates a second function. The function returns the combined scope of itself and the one returned by calling c.
			cc = () => {
				f = b => b;
				this + c( 6 );
			};
			// Calls cc and receives the new scope. The scope overrides f from the c scope.
			oo = cc();
			// Calls f in the scope oo and returns 3.
			if( ( got = oo.f( 3 ) ) != 3 )
				throw( "Expected 3, got ${got}" );
			'''
		);
	}

	@Test
	static public void test4()
	{
		fail( '''
			f = () => (
				throw( "error" )
			);
			f();
			''',
			ScriptException, "error"
		)
		// assert( actual( o.f( 3 ) ) == expected( 12 ) ); // Will throw: Expected 12, got ${got}
		// assert( x != null ); // Will throw: x must not be null
		// assert( x ); // Will throw: x must not be false/null/empty, depending on what case is found
		// TODO assert with multiple tuples?
	}

	@Test
	static public void test5()
	{
		eval( '''
			l = ArrayList.getMethods().map( method => method.getName() );
			l.foreach( name => println( name ) );
			'''
		);
	}
}
