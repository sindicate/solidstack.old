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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.SourceException;
import solidstack.script.java.Java;
import solidstack.script.objects.FunnyString;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.ScopeException;
import solidstack.script.scopes.Symbol;
import solidstack.script.scopes.TempSymbol;


@SuppressWarnings( "javadoc" )
public class ScriptTests extends Util
{
	@Test
	static public void test1()
	{
		Scope context = new Scope();
		context.set( "var1", "Value" );
		test( "var1", context, "Value" );
		test( "", null );
	}

	@Test
	static public void test2()
	{
		Scope context = new Scope();
		context.set( "var1", 1 );
		test( "var1 + 1", context, 2 );
	}

	@Test
	static public void test3()
	{
		test( "1 + 1 * 2", 3 );
		test( "2 * 1 + 1", 3 );
		test( "1 + 1 + 1", 3 );
		test( "1 + 2 * 2 + 1", 6 );
		test( "( 1 + 2 ) * 2 + 1", 7 );
		test( "1 + 2 * ( 2 + 1 )", 7 ); // TODO These () translate to Tuple instead of Parenthesis
		test( "( 1 + 2 ) * ( 2 + 1 )", 9 );
	}

//	@Test
//	static public void test4()
//	{
//		int val = 1;
//		Assert.assertEquals( val > 0 ? 2 : 3 + 1, 2 );
//		Assert.assertEquals( val + 1 > 0 ? 2 : 3 + 1, 2 );
//		Assert.assertEquals( val > 0 ? 2 : val > 0 ? 3 : 4, 2 );
//
//		testParseFail( "1 ? 2 ; 3 : 4" );
//		test( "1 ? 2 : 3 + 1", 2 );
//		test( "( 1 ? 2 : 3 ) + 1", 3 );
//		test( "1 + 1 ? 2 : 3 + 1", 2 );
//		test( "1 + ( 1 ? 2 : 3 ) + 1", 4 );
//		test( "1 + ( 1 ? 2 : 3 + 1 )", 3 );
//		test( "( 1 + 1 ? 2 : 3 ) + 1", 3 );
//		test( "0 ? 2 : 3 + 4 * 5", 23 );
//		test( "0 ? 2 : ( 3 + 4 ) * 5", 35 );
//		test( "( 0 ? 2 : 3 ) + 4 * 5", 23 );
//		test( "( ( 0 ? 2 : 3 ) + 4 ) * 5", 35 );
//		test( "1 ? 1 ? 2 : 3 : 4", 2 );
//		test( "1 ? 0 ? 2 : 3 : 4", 3 );
//		test( "0 ? 0 ? 2 : 3 : 4", 4 );
//		test( "1 ? 2 : 1 ? 3 : 4", 2 );
//		test( "( 1 ? 2 : 1 ) ? 3 : 4", 3 );
//		test( "0 ? 2 : 1 ? 3 : 4", 3 );
//		test( "0 ? 2 : 0 ? 3 : 4", 4 );
//		test( "1 ? 2 : 3 + 4 ? 5 : 6", 2 );
//		test( "( 1 ? 2 : 3 ) + 4 ? 5 : 6", 5 );
//		test( "1 ? 2 : 3 + ( 4 ? 5 : 6 )", 2 );
//		test( "( 1 ? 2 : 3 ) + ( 4 ? 5 : 6 )", 7 );
//		test( "0 ? 2 : 3 + 4 ? 5 : 6", 5 );
//		test( "0 ? 2 : 3 + ( 4 ? 5 : 6 )", 8 );
//
//		test( "1 + 1 == 2 ? 2 : 3", 2 );
//		test( "!0 ? 2 : 3", 2 );
//	}

	@Test
	static public void test4()
	{
		test( "if( 1 ) 2 else 3 + 1", 2 );
		test( "if( !\"\" ) 2 else 3", 2 );
		test( "if( !\"x\" ) 2 else 3", 3 );
		test( "if( 1 ) 2", 2 );
		test( "if( 1 ) () else 2", null );
		test( "if( null ) 2", null );
		test( "if( null ) () else 2", 2 );
		test( "1 || 2", 1 );
		test( "0 || 2", 0 );
		test( "null || 2", 2 );
		test( "1 && 2", 2 );
		test( "0 && 2", 2 );
		test( "null && 2", null );
		test( "1 && 2 || 3 + 1", 2 );
		test( "!\"\" && 2 || 3", 2 );
		test( "!\"x\" && 2 || 3", 3 );
	}

	@Test
	static public void test5()
	{
		Scope context = new Scope();

		test( "a = 1", context, 1 );
		Assert.assertEquals( context.get( "a" ), 1 );

		test( "a = b = 1", context, 1 );
		Assert.assertEquals( context.get( "a" ), 1 );
		Assert.assertEquals( context.get( "b" ), 1 );

		test( "1 + ( a = 1 )", context, 2 );
		Assert.assertEquals( context.get( "a" ), 1 );

		test( "1 + ( a = 1 ) + a", context, 3 );
		Assert.assertEquals( context.get( "a" ), 1 );
	}

	@Test
	static public void test6()
	{
		test( "\"test\"", "test" );
		test( "\"test\" + \"test\"", "testtest" );
	}

	@Test
	static public void test7()
	{
		test( "abs( +1 )", 1 );
		test( "abs( -1 )", 1 );
		test( "abs( 1 + -2 )", 1 );

		test( "substr( \"sinterklaas\", 1 + 2 * 1, 9 - 1 - 1 )", "terk" );
		test( "substr( \"sinterklaas\", 6 )", "klaas" );
		test( "upper( \"sinterklaas\" )", "SINTERKLAAS" );
		test( "println( \"Hello World!\" )", "Hello World!" );
		test( "println( upper( \"Hello World!\" ) )", "HELLO WORLD!" );
		test( "length( \"sinterklaas\" )", 11 );
		test( "\"sinterklaas\".length()", 11 );
		test( "\"sinterklaas\".size()", 11 );
		test( "defined( a )", false );
		test( "defined( def( a ) )", true );
		test( "a = null; defined( a )", true );
		test( "if( a ) a", null ); // TODO Ponder over this once more
		test( "a = null; a && a", null );
		test( "a && 1 || 2", 2 );
	}

	@Test
	static public void test8()
	{
		test( "1 == 1", true );
		test( "1 == 0", false );

		test( "true", true );
		test( "false", false );
		test( "1 == 1 == true", true );
		test( "true == 1 == 1", false );
		test( "true == ( 1 == 1 )", true );

		test( "!true", false );
		test( "!false", true );
		test( "!( 1 == 1 )", false );
		test( "!( 1 == 0 )", true );
		test( "!1 == false", true );

		test( "false && false", new Boolean( false ) );
		test( "false && true", new Boolean( false ) );
		test( "true && false", new Boolean( false ) );
		test( "true && true", new Boolean( true ) );

		test( "false || false", new Boolean( false ) );
		test( "false || true", new Boolean( true ) );
		test( "true || false", new Boolean( true ) );
		test( "true || true", new Boolean( true ) );

		test( "println( true ) && println( true )", new Boolean( true ) );
	}

	@Test
	static public void test9()
	{
		test( "null", null );
		test( "null == null", true );
		test( "1 == null", false );
		test( "\"test\" == null", false );
	}

	@Test
	static public void test10()
	{
		test( "2; 3", 3 );
		test( "a = 0; b = 1", 1 );
		test( "a = 0;", 0 );
		test( ";", null );
		test( "", null );
		test( ";;; a = 0;;;; b = 1;;;", 1 );
		test( ";;;;", null );
	}

	@Test
	static public void test11()
	{
		test( "( 2; 3 )", 3 );
		test( "a = 1; a + a + a++", 3 );
		test( "a = 1; a + a + ++a", 4 );
		// TODO If should stop at the comma just as with the ;
		test( "a = 0; ( b, c ) = if( true ) ( a++, a++ )", Arrays.asList( 0, 1 ) );
		test( "if( a = 1, b = a, b ) 3 else 4", 3 );
		test( "if( a = null, b = a, b ) 3 else 4", 4 );
		test( "a = 0; ( b, c ) = if( false ) ( a++, a++ ) else ( ++a, ++a )", Arrays.asList( 1, 2 ) );
		test( "i = 0; while( i < 10 ) print( i++ )", 9 );
		test( "i = 0; while( i++ < 10 ) print( i )", 10 );
		test( "i = 0; while( i++ < 10 && print( i ) ) ()", null ); // TODO Is there an example where result of condition should be returned?
	}

	@Test
	static public void test12()
	{
		testParseFail( "println( 1 " );
		test( "f = fun( a; a * a ); f( 3 )", 9 );
		// TODO New fun syntax
		test( "fun( a; a * a ) ( 5 )", 25 );
		test( "b = 8; fun( a; a ) ( b )", 8 );
		test( "fun( a; a( 3 ) ) ( fun( b; 5 * b ) )", 15 );
		test( "fun( a, b; a( 1, 2 ) * b( 3, 4 ) ) ( fun( c, d; c * d ), fun( e, f; e * f ) )", 24 );
		test( "fun( a, b; a( 1, 2 ) * b( 3, 4 ) ) ( fun( a, b; a * b ), fun( a, b; a * b ) )", 24 );
		test( "f = fun( ; 1 ); f()", 1 );
		test( "a = 0; fun( ; a = 1 ) (); a", 1 );
		test( "fun( a; a ) ( null )", null );
		test( "f = fun( ; fun( ; 2 ) ); f()()", 2 );
		test( "a = 1; f = fun( ; a ); a = 2; f()", 2 );
		test( "fun(a;fun(;a))(1)()", 1 );
	}

	@Test
	static public void test13()
	{
		Scope context = new Scope();
		context.set( "s", "sinterklaas" );
		test( "s.length()", context, 11 );
		test( "s.substring( 6 )", context, "klaas" );
		test( "s.substring( 1, 6 )", context, "inter" );
		test( "s.contains( \"kl\" )", context, true );

		TestObject1 o1 = new TestObject1();
		context.set( "o1", o1 );
		test( "o1.test()", context, 0 );
		test( "o1.test( 1.0 )", context, 2 );
		test( "o1.test( \"string\" )", context, 3 );
		test( "o1.test( \"string\", \"string\" )", context, 4 );
		assert o1.test( new BigDecimal( 1 ), new BigDecimal( 1 ) ) == 6;
		test( "o1.test( 1, 1 )", context, 6 );
		test( "1.getClass()", Integer.class );
		test( "1.1.getClass()", BigDecimal.class );
		test( "1.0.getClass()#valueOf( 1.1 as double )", new BigDecimal( "1.1" ) );
		test( "1.00.valueOf( 1.1 as double )", new BigDecimal( "1.1" ) );
		test( "o1.test( 1 == 1 )", context, 7 );
		test( "o1.test( a -> 1, b -> 2 )", context, 8 );

		TestObject2 o2 = new TestObject2();
		context.set( "o2", o2 );
		test( "o2.test( 1, 1 )", context, 1 );
	}

	@Test
	static public void test13_2()
	{
		Scope context = new Scope();
		test( "c = class( \"solidstack.script.ScriptTests$TestObject1\" );", context, TestObject1.class );
		test( "new c().value", context, 0 );
		test( "new c( 3.14 ).value", context, 2 );
		test( "new c( 0.123E-10 ).value", context, 2 );
		test( "new c( \"string\" ).value", context, 3 );
		test( "new c( \"string\", \"string\" ).value", context, 4 );
		test( "new c( 1, 1 ).value", context, 6 );
		test( "new c( 1 == 1 ).value", context, 7 );
		test( "new c( a -> 1, b -> 2 ).value", context, 8 );

		test( "c2 = class( \"solidstack.script.ScriptTests$TestObject2\" );", context, TestObject2.class );
		test( "new c2( 1, 1 ).value", context, 1 );
	}

	@Test
	static public void test14()
	{
		test( "( a, b ) = ( 1, 2 ); a + b", 3 );
		test( "( a, b ) = ( 1, 2 ); a + b", 3 );
		test( "( a, b ) = fun( ; 1, 2 )(); a + b", 3 );
		test( "( a, b ) = ( fun( ; 1 ), fun( ; 2 ) ) ; a() + b()", 3 );
		test( "( a, b ) = ( () => ( 1, 2 ) )(); a + b", 3 );
		test( "( a, b ) = ( () => 1, () => 2 ); a() + b()", 3 );
	}

	@Test
	static public void test15()
	{
		test( "a = 1; \"a = ${a}\"", "a = 1" );
		test( "a = 1; s = \"a = ${a}\"; a = 2; s", "a = 1" );
		test( "a = 1; \"a = \\${a}\"", "a = ${a}" );
		test( "\"${1}\"", "1" );
//		test( "\"${}\"", "1" ); TODO
		test( "\"\".getClass()", String.class );
		test( "\"x\".getClass()", String.class );
		test( "\"${1}\".getClass()", FunnyString.class );
		test( "\"x${1}x\".getClass()", FunnyString.class );
		test( "\"x${1}x\".size()", 3 );
	}

	@Test
	static public void test16()
	{
		test( "def( a ) = 1;", 1 );

		test( "fun( ; a = 1 )(); a", 1 ); // The function has no context of its own
		test( "a = 1; fun( a; a++ )( a ); a;", 1 );
		test( "a = 1; fun( ; def( a ) = 2 )(); a", 2 ); // The function has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", 2 ); // The function has no context of its own
		test( "a = 1; fun{ ; def( a ) = 2 }(); a", 1 ); // The function has its own context

		test( "( a = 1 ); a", 1 ); // The block has no context of its own
		test( "a = 1; ( def( a ) = 2 ); a", 2 ); // The block has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", 2 ); // The function has no context of its own
		test( "a = 1; { def( a ) = 2 }; a", 1 ); // The block has its own context

		test( "( () => a = 1 )(); a", 1 ); // The function has no context of its own
		test( "a = 1; ( a => a++ )( a ); a;", 1 );
		test( "a = 1; ( () => def( a ) = 2 )(); a", 2 ); // The function has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", 2 ); // The function has no context of its own
		test( "a = 1; f = () => { def( a ) = 2 }; f(); a", 1 ); // The function has its own context
	}

	@Test
	static public void test17()
	{
		test( "class( \"java.util.ArrayList\" );", ArrayList.class );
		test( "c = class( \"java.util.ArrayList\" ); new c();", new ArrayList<Object>() );
		test( "l = new ( class( \"java.util.ArrayList\" ) )(); l.add( \"sinterklaas\" ); l.toArray();", new Object[] { "sinterklaas" } );
		test( "ArrayList = class( \"java.util.ArrayList\" ); l = new ArrayList(); l.toArray();", new Object[ 0 ] );
	}

	@Test
	static public void test18()
	{
		test( "l = new ( class( \"java.util.ArrayList\" ) )(); i = 0; while( i < 10 ) ( l.add( i ); i++ ); l.each( fun( i; println( i ) ) )", 9 );
		eval( "Calendar = class( \"java.util.Calendar\" ); println( Calendar#getInstance().getClass() )" );
		test( "Calendar = class( \"java.util.Calendar\" ); Calendar#SATURDAY", 7 );
		fail( "Calendar = class( \"java.util.Calendar\" ); Calendar#clear()", ScriptException.class, "static java.util.Calendar.clear()" );
		fail( "TestObject = class( \"solidstack.script.ScriptTests$TestObject2\" ); TestObject#value", ScriptException.class, "static solidstack.script.ScriptTests$TestObject2.value" );
	}

	@Test
	static public void test19()
	{
		try
		{
			eval( "o = class( \"solidstack.script.ScriptTests$TestObject3\" )(); o.throwException()" );
		}
		catch( Exception e )
		{
			e.printStackTrace( System.out );
		}
	}

	@Test
	static public void test20() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Object test = new String[ 10 ][ 10 ];
		assert test instanceof Object[];
		assert ( (Object[])test )[ 0 ] instanceof Object[];

		assert test.getClass().getSuperclass() == Object.class;
		assert Object[].class.isAssignableFrom( test.getClass() );

		ClassLoader loader = ScriptTests.class.getClassLoader();
		Assert.assertEquals( Java.forName( "java.lang.Object", loader ), Object.class );
		Assert.assertEquals( Java.forName( "java.lang.Object[]", loader ), Object[].class );
		Assert.assertEquals( Java.forName( "java.lang.Object[][]", loader ), Object[][].class );
		Assert.assertEquals( Java.forName( "int", loader ), int.class );
		Assert.assertEquals( Java.forName( "int[]", loader ), int[].class );
		Assert.assertEquals( Java.forName( "int[][][][]", loader ), int[][][][].class );
		Assert.assertEquals( Java.forName( "int[][]", loader ), int[][].class );

		test( "list = List( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ); list( 3 )", 4 );
		test( "list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ].toArray(); list[ 3 ]", 4 );
		test( "list = []; list.size()", 0 );

		test( "map = [ 0 -> 1, 1 -> 2, 2 -> 3, 3 -> 4 ]; map[ 3 ]", 4 );
		test( "map = [ \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ]; map[ \"second\" ]", 2 );
		test( "map = [ \"fir\" + \"st\" -> 1, \"second\" -> 2, \"third\" -> 3 ]; map[ \"first\" ]", 1 );
		test( "map = [ \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ]; map[ \"fourth\" ]", null ); // TODO Undefined? Then we assign to it too.
		test( "map = scope( [ \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ] ); map.third", 3 );
		test( "map = scope( [ \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ] ); map.fourth", null ); // TODO What about undefined?
		test( "map = [:]; s = scope( map ); s.first = 1; map[ \"first\" ]", 1 );
		test( "map = [:]; map[ \"fourth\" ]", null );
		test( "map = [:]; map.size()", 0 );
		test( "map = [:]; map[ \"third\" ] = 3; map[ \"third\" ]", 3 );

		test( "array = class( \"java.lang.reflect.Array\" )#newInstance( class( \"java.lang.String\" ), 10 ); array.size()", 10 );
		test( "array = class( \"java.lang.reflect.Array\" )#newInstance( class( \"int\" ), 10 ); array.size()", 10 );

		eval( "fun( a; a )( null )" );
		eval( "fun( a; a )( if( false; 1 ) )" );
		eval( "fun( a; a )( while( false ) 1 )" );
		eval( "fun( a; a )( [].each( fun( a; () ) ) )" );

		eval( "( a => a )( null )" );
		eval( "( a => a )( if( false; 1 ) )" );
		eval( "( a => a )( while( false ) 1 )" );
		eval( "( a => a )( [].each( a => () ) )" );
	}

	@Test
	static public void test21()
	{
		test( "f = a => a; f( 3 )", 3 );
		test( "f = a => a * a; f( 3 )", 9 );
		test( "f = ( a ) => ( a * a ); f( 3 )", 9 );
		test( "( a => a * a )( 5 )", 25 );
		test( "( a => a( 3 ) ) ( b => 5 * b )", 15 );
		test( "( ( a, b ) => a( 1, 2 ) * b( 3, 4 ) ) ( ( c, d ) => c * d, ( e, f ) => e * f )", 24 );
		test( "( ( a, b ) => a( 1, 2 ) * b( 3, 4 ) ) ( ( a, b ) => a * b, ( a, b ) => a * b )", 24 );
		test( "f = () => 1; f()", 1 );
		test( "a = 0; ( () => a = 1 ) (); a", 1 );
		test( "( a => a ) ( null )", null );
		test( "f = () => () => 2; f()()", 2 );
		test( "a = 1; f = () => a; a = 2; f()", 2 );
		test( "( a => () => a )( 1 )()", 1 );

		test( "f = (a,b)=>a*b; g = a=>f(a,3); g(5)", 15 );
		test( "s = \"string\"; c = x=>s.charAt(x); c(2)", 'r' );
		test( "f = () => (1,2,3); (a,b,c) = f(); a+b+c;", 6 );

		test( "l = [ 1, 2, 3 ]; l.each( i => println( i ) )", 3 );
	}

	@Test
	static public void test22()
	{
		test( "f = (a,b,c) => a+b+c; g = (*a) => f(*a); g(1,2,3)", 6 );
		test( "f = *a => \"sinterklaas\".charAt( *a ); f( 1 )", 'i' );
		test( "Arrays = class( \"java.util.Arrays\" ); asList = *i => Arrays#asList( *i ); list = asList( 1, 2, 3 ); list.size()", 3 );
		test( "f = ( a, *b ) => b.size(); f( 1, 2, 3 )", 2 );
		test( "f = ( a, *b ) => a; g = ( a, *b ) => f( *b, a ); g( 1, 2, 3 )", 2 );
		test( "f = *a => a.size(); f( 1, 2, 3 );", 3 );
		test( "l = [1,2,3]; f = (a,b,c) => a+b+c; f(*l);", 6 );

		test( "( a, b, c ) = *[ 1, 2, 3 ]; a + b + c", 6 );
		test( "a = [ 1, 2, 3 ]; ( b, c, d ) = *a; b + c + d", 6 );
		test( "( 1, 2, 3 ).list().size()", 3 );
//		test( "*a = ( 1, 2, 3 ); a.size()", 3 ); // TODO
//		test( "( a, *b ) = ( 1, 2, 3 )", 3 ); TODO
//		test( "( a, *b ) = ( *[ 1, 2 ], 3 )", 3 ); TODO
		test( "a = [ 1, [ 2, 3, 4 ], 5 ]; ( (a,b,c) => a+b+c )( *a[ 1 ] )", 9 );

		fail( "f = a => (); f( 1, 2, 3 );", ScriptException.class, "Too many parameters" );
		fail( "a = *[ 1, 2, 3 ]; ( b, c, d ) = a; b + c + d", ScriptException.class, "Can't assign tuples to variables" );
		fail( "a = ( 1, 2, 3 ); ( b, c, d ) = a; b + c + d", ScriptException.class, "Can't assign tuples to variables" );

		// TODO Key value tuples for named parameters?
	}

	@Test
	static public void test23()
	{
		test( "f = (a,b,c) => a+b+c; f( a -> 1, b -> 2, c -> 3 )", 6 );
	}

	@Test
	static public void test24()
	{
		Symbol real1 = Symbol.forString( "symbol" );
		Symbol real2 = Symbol.forString( "symbol" );
		Symbol temp1 = new TempSymbol( "symbol" );
		Symbol temp2 = new TempSymbol( "symbol" );
		assertThat( real1 ).isSameAs( real2 );
		assertThat( real1 ).isEqualTo( real2 );
		assertThat( real1 ).isEqualTo( temp1 );
		assertThat( temp1 ).isEqualTo( real1 );
		assertThat( temp1 ).isEqualTo( temp2 );

		test( "s = :symbol; s.toString()", "symbol" );
		test( "s = :\"dit is ook een symbol\"; s.toString()", "dit is ook een symbol" );
		test( "s = :red; if( s == :red ) true else false", true );
	}

	@Test
	static public void test25()
	{
//		test( "a as boolean", false ); // TODO Should this fail or give 'false'?
		test( "null as boolean", false );
		test( "0 as boolean", true );
		test( "1 as boolean", true );
		test( "false as boolean", false );
		test( "true as boolean", true );
		test( "\"\" as boolean", false );
		test( "\"x\" as boolean", true );
		test( "[] as boolean", false );
		test( "[1] as boolean", true );
		test( "[:] as boolean", false );
		test( "[1 ->1] as boolean", true );

		test( "1 as byte", (byte)1 );
		test( "a = 1; a as byte", (byte)1 );
		test( "a = 1 as byte", (byte)1 );
		test( "1 as char", (char)1 );
		test( "1 as short", (short)1 );
		test( "1 as int", 1 );
		test( "1 as long", 1L );
		test( "1.1 as float", (float)1.1 );
		test( "1.1 as double", 1.1 );

		test( "1 as BigInteger", new BigInteger( "1" ) );

		test( "1 as BigInteger", new BigInteger( "1" ) );
		test( "1 as BigDecimal", new BigDecimal( "1" ) );
		test( "1 as String", "1" );
		fail( "\"1\" as int", ClassCastException.class, "java.lang.String cannot be cast to java.lang.Number" ); // TODO Should be int instead of Number

		test( "1 instanceof Integer", true );
		test( "1 instanceof int", false );
		test( "\"1\" instanceof class( \"java.lang.CharSequence\" )", true );
		test( "\"1\" instanceof String", true );
		test( "null instanceof Byte", false );
	}

	@Test
	static public void test26()
	{
		test( "1 as byte + 1 as byte", 2 );
		test( "1 as byte + 1", 2 );
		test( "1 as byte + 1 as long", 2L );
		test( "1 as byte + 1 as BigInteger", new BigInteger( "2" ) );
		test( "1 as byte + 1 as float", 2f );
		test( "1 as byte + 1 as double", 2d );
		test( "1 as byte + 1 as BigDecimal", new BigDecimal( "2" ) );

		test( "1 + 1 as byte", 2 );
		test( "1 as long + 1 as byte", 2L );
		test( "1 as BigInteger + 1 as byte", new BigInteger( "2" ) );
		test( "1 as float + 1 as byte", 2f );
		test( "1 as double + 1 as byte", 2d );
		test( "1 as BigDecimal + 1 as byte", new BigDecimal( "2" ) );

		test( "1 as char + 1 as char", 2 );
		test( "1 as char + 1 as long", 2L );

		test( "-1 as char + -1 as char", 0x1FFFE );
		test( "-1 as byte + -1 as byte", -2 );

		test( "16 as byte - 16 as byte", 0 );
		test( "16 as byte * 16 as byte", 256 );
		test( "16 as byte / 16 as byte", 1 );
		test( "16 as byte % 15 as byte", 1 );

		assertThat( -3 % 2 ).isEqualTo( -1 ); // So this is not mod but remainder
		assertThat( new BigInteger( "-3" ).mod( new BigInteger( "2" ) ) ).isEqualTo( new BigInteger( "1" ) );
		assertThat( new BigInteger( "-3" ).remainder( new BigInteger( "2" ) ) ).isEqualTo( new BigInteger( "-1" ) );

		test( "16 as float / 15 as byte", (float)16 / 15 );
		test( "16 as float % 15 as byte", (float)1 );

		test( "-15 as byte", (byte)-15 );
		test( "-15 as char", (char)-15 );
		test( "-(15 as byte)", -15 );
		test( "-(15 as char)", -15 );
		test( "-(1.5 as float)", -1.5f );

		test( "abs( -15 as byte )", 15 );
		test( "abs( -15 as char )", 65521 );
		test( "abs( -15 )", 15 );
		test( "abs( -1.5 as float )", 1.5f );

		test( "1 as byte < 2 as byte", true );
		test( "1 as char < 2 as char", true );
		test( "1 as short < 2 as short", true );
		test( "1 as int < 2 as int", true );
		test( "1 as long < 2 as long", true );
		test( "1 as float < 2 as float", true );
		test( "1 as double < 2 as double", true );
		test( "1 as BigInteger < 2 as BigInteger", true );
		test( "1 as BigDecimal < 2 as BigDecimal", true );

		test( "1 as byte < 2 as byte", true );
		test( "1 as byte < 2 as char", true );
		test( "1 as byte < 2 as short", true );
		test( "1 as byte < 2 as int", true );
		test( "1 as byte < 2 as long", true );
		test( "1 as byte < 2 as float", true );
		test( "1 as byte < 2 as double", true );
		test( "1 as byte < 2 as BigInteger", true );
		test( "1 as byte < 2 as BigDecimal", true );

		test( "1 as byte == 1 as byte", true );
		test( "1 as byte == 1 as char", true );
		test( "1 as byte == 1 as short", true );
		test( "1 as byte == 1 as int", true );
		test( "1 as byte == 1 as long", true );
		test( "1 as byte == 1 as float", true );
		test( "1 as byte == 1 as double", true );
		test( "1 as byte == 1 as BigInteger", true );
		test( "1 as byte == 1 as BigDecimal", true );
	}

	@Test
	static public void test27()
	{
		Scope scope = new Scope();
		scope.set( "o1", new TestObject1() );

		fail( "class( \"xxx\" )", ScriptException.class, "No such class: xxx" );
		fail( "1.xxx", ScriptException.class, "No such field: java.lang.Integer.xxx" );
		fail( "class( \"java.lang.Integer\" )#xxx", ScriptException.class, "No such field: static java.lang.Integer.xxx" );
		fail( "o1.test( null )", scope, ScriptException.class, "test(java.util.Map)" );
		fail( "f = ( *b, c ) => (); f()", ScriptException.class, "Collecting parameter must be the last parameter" );
		fail( "f = ( a ) => (); f()", ScriptException.class, "Not enough parameters" );
		fail( "f = () => (); f( 1 )", ScriptException.class, "Too many parameters" );
		fail( "f()", ScopeException.class, "'f' undefined" );
		fail( "f = null; f()", ScriptException.class, "Function is null" );
		fail( "f = 1; f()", ScriptException.class, "No such method: java.lang.Integer.apply()" );
		fail( "a = ( 1, 2 )", ScriptException.class, "Can't assign tuples to variables" );
		fail( "f = null; f[]", ScriptException.class, "Null can't be indexed" );
		fail( "f = 1; f[]", ScriptException.class, "Missing index" );
		fail( "f = 1; f[ 1 ]", ScriptException.class, "Can't index a java.lang.Integer" );
		fail( "--1", ScriptException.class, "Can't apply -- to a java.lang.Integer" );
		fail( "++1", ScriptException.class, "Can't apply ++ to a java.lang.Integer" );
		fail( "1--", ScriptException.class, "Can't apply -- to a java.lang.Integer" );
		fail( "1++", ScriptException.class, "Can't apply ++ to a java.lang.Integer" );
		fail( "--null", ScriptException.class, "Can't apply -- to a null" );
		fail( "++null", ScriptException.class, "Can't apply ++ to a null" );
		fail( "null--", ScriptException.class, "Can't apply -- to a null" );
		fail( "null++", ScriptException.class, "Can't apply ++ to a null" );
		fail( "[ a: 1, 2 ]", ScriptException.class, "All items in a map must be labeled" );
		fail( ":1", SourceException.class, "Symbol must be an identifier or a string" );
		fail( "abs()", ScriptException.class, "abs() needs exactly one parameter" );
		fail( "class()", ScriptException.class, "class() needs exactly one parameter" );
		fail( "class( 1 )", ScriptException.class, "class() needs a string parameter" );
		fail( "def()", ScriptException.class, "def() needs exactly one parameter" );
		fail( "def( 1 )", ScriptException.class, "def() needs a variable identifier as parameter" );
		fail( "defined()", ScriptException.class, "defined() needs exactly one parameter" );
		fail( "defined( 1 )", ScriptException.class, "defined() needs a variable identifier as parameter" );
		fail( "length()", ScriptException.class, "length() needs exactly one parameter" );
		fail( "length( 1 )", ScriptException.class, "length() needs a string parameter" );
		fail( "print()", ScriptException.class, "print() needs exactly one parameter" );
		fail( "println()", ScriptException.class, "println() needs exactly one parameter" );
		fail( "scope()", ScriptException.class, "scope() needs exactly one parameter" );
		fail( "scope( 1 )", ScriptException.class, "scope() needs a map parameter" );
		fail( "stripMargin()", ScriptException.class, "stripMargin() needs exactly one parameter" );
		fail( "stripMargin( 1 )", ScriptException.class, "stripMargin() needs a string parameter" );
		fail( "substr()", ScriptException.class, "substr() needs 2 or 3 parameters" );
		fail( "substr( 1, 1 )", ScriptException.class, "substr() needs a string as first parameter" );
		fail( "substr( \"\", \"\" )", ScriptException.class, "substr() needs an integer as second parameter" );
		fail( "substr( \"\", 1, \"\" )", ScriptException.class, "substr() needs an integer as third parameter" );
		fail( "throw()", ScriptException.class, "throw() needs exactly one parameter" );
		fail( "upper()", ScriptException.class, "upper() needs exactly one parameter" );
		fail( "upper( 1 )", ScriptException.class, "upper() needs a string parameter" );
		fail( "val()", ScriptException.class, "val() needs exactly one parameter" );
		fail( "val( 1 )", ScriptException.class, "val() needs a variable identifier as parameter" );
		fail( "f = ( a ) => (); f( b -> 1 )", ScriptException.class, "Parameter 'b' undefined" );
		fail( "f = ( a, b ) => (); f( a -> 1, 2 )", ScriptException.class, "All parameters must be named" );
		fail( "f = ( a ) => (); f( \"a\" -> 1 )", ScriptException.class, "Parameter must be named with a variable identifier" );
	}

	@Test
	static public void test28()
	{
		test( "if( true; return( true ) ); return( false )", true );
		test( "l = [ 1, 2, 3 ]; l.each( i => return( i ) ); 4", 1 );
		test( "l = [ 1, 2, 3 ]; f = i => return( i ); l.each( f ); 4", 4 );
	}

	@Test
	static public void prims() throws IOException
	{
		String script = readFile( "Prim's Minimum Spanning Tree.funny" );
		eval( script );
	}

	@Test
	static public void sql() throws IOException
	{
		String script = readFile( "SqlTests.funny" );
		eval( script );
	}

	// DONE Calls with named parameters
	// TODO A function without parameters, does not need the FunctionObject. Its just an unevaluated expression.
	// TODO Exceptions, catch & finally
	// TODO MethodMissing
	// TODO Default parameter values
	// TODO def & val
	// TODO Store tuples in variables?
	// TODO Binary and hexadecimal literals
	// DONE Add methods to the datatypes and/or objects
	// TODO DSLs
	// TODO Underscores in number literals
	// DONE Spread parameters or collection access
	// DONE Arrays and maps with literals
	// TODO Ranges
	// TODO Synchronization
	// TODO Return, switch, break, continue
	// TODO Threads & sleep, etc
	// TODO Assert with lazy evaluation of its arguments
	// TODO Optional? Lazy evaluation of all arguments
	// DONE // Comments, /* comments
	// TODO /** comments which can contain /* comments
	// TODO Compile time (post processing) transformation functions, for example: removeMargins()
	// TODO Token interceptors that work on the token stream, or custom script parsers for eval
	// DONE Symbols :red
	// TODO Mixins
	// TODO Lazy evaluation
	// TODO Class extension pluggable
	// TODO Extensions: unique/each(WithIndex)/find(All)/collect/contains/every/indexOf/flatten/groupBy/inject/join/max/min/removeAll/replaceAll/reverse/sum/tail/traverse/withReader(etc)
	// TODO with() to execute a function with a different context
	// DONE Currying, no need
	// TODO Global namespaces
	// TODO Operator calling method on first operand, operator overloading
	// TODO Hints for null parameters: a as String which evaluates to a TypedNull object if a is null
	// TODO Compilation errors including column number
	// TODO Compartmentalization like Java does with classloaders. This means name spaces too.
	// TODO Adding tuples or appending values to it.
	// TODO Always remember the lexical scope. Needed if we want script file specific settings.

	@SuppressWarnings( "unused" )
	static public class TestObject1
	{
		public int value;

		public TestObject1() { this.value = 0; }
		public TestObject1( int i ) { this.value = 1; }
		public TestObject1( BigDecimal i ) { this.value = 2; }
		public TestObject1( String s ) { this.value = 3; }
		public TestObject1( String... s ) { this.value = 4; }
		public TestObject1( BigDecimal... b ) { this.value = 5; }
		public TestObject1( BigDecimal b1, Number b2 ) { this.value = 6; }
		public TestObject1( boolean b ) { this.value = 7; }
		public TestObject1( Map args ) { this.value = 8; }

		public int test() { return 0; }
		public int test( int i ) { return 1; }
		public int test( BigDecimal i ) { return 2; }
		public int test( String s ) { return 3; }
		public int test( String... s ) { return 4; }
		public int test( BigDecimal... b ) { return 5; }
		public int test( BigDecimal b1, Number b2 ) { return 6; }
		public int test( boolean b ) { return 7; }
		public int test( Map args ) { return 8; }
	}

	@SuppressWarnings( "unused" )
	static public class TestObject2
	{
		public int value;

		public TestObject2() { this.value = 0; }
		public TestObject2( int i1, int i2 ) { this.value = 1; }

		public int test( int i1, int i2 ) { return 1; }
	}

	@SuppressWarnings( "unused" )
	static public class TestObject3
	{
		public void throwException() throws Exception
		{
			throw new Exception( "test exception" );
		}
	}
}
