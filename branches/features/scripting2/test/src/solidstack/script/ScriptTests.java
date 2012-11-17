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
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.SourceException;
import solidstack.script.java.Java;
import solidstack.script.java.MissingFieldException;
import solidstack.script.java.MissingMethodException;
import solidstack.script.objects.FunnyString;
import solidstack.script.scopes.Scope;


@SuppressWarnings( "javadoc" )
public class ScriptTests
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
		context.set( "var1", new BigDecimal( 1 ) );
		test( "var1 + 1", context, new BigDecimal( 2 ) );
	}

	@Test
	static public void test3()
	{
		test( "1 + 1 * 2", new BigDecimal( 3 ) );
		test( "2 * 1 + 1", new BigDecimal( 3 ) );
		test( "1 + 1 + 1", new BigDecimal( 3 ) );
		test( "1 + 2 * 2 + 1", new BigDecimal( 6 ) );
		test( "( 1 + 2 ) * 2 + 1", new BigDecimal( 7 ) );
		test( "1 + 2 * ( 2 + 1 )", new BigDecimal( 7 ) ); // TODO These () translate to Tuple instead of Parenthesis
		test( "( 1 + 2 ) * ( 2 + 1 )", new BigDecimal( 9 ) );
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
//		test( "1 ? 2 : 3 + 1", new BigDecimal( 2 ) );
//		test( "( 1 ? 2 : 3 ) + 1", new BigDecimal( 3 ) );
//		test( "1 + 1 ? 2 : 3 + 1", new BigDecimal( 2 ) );
//		test( "1 + ( 1 ? 2 : 3 ) + 1", new BigDecimal( 4 ) );
//		test( "1 + ( 1 ? 2 : 3 + 1 )", new BigDecimal( 3 ) );
//		test( "( 1 + 1 ? 2 : 3 ) + 1", new BigDecimal( 3 ) );
//		test( "0 ? 2 : 3 + 4 * 5", new BigDecimal( 23 ) );
//		test( "0 ? 2 : ( 3 + 4 ) * 5", new BigDecimal( 35 ) );
//		test( "( 0 ? 2 : 3 ) + 4 * 5", new BigDecimal( 23 ) );
//		test( "( ( 0 ? 2 : 3 ) + 4 ) * 5", new BigDecimal( 35 ) );
//		test( "1 ? 1 ? 2 : 3 : 4", new BigDecimal( 2 ) );
//		test( "1 ? 0 ? 2 : 3 : 4", new BigDecimal( 3 ) );
//		test( "0 ? 0 ? 2 : 3 : 4", new BigDecimal( 4 ) );
//		test( "1 ? 2 : 1 ? 3 : 4", new BigDecimal( 2 ) );
//		test( "( 1 ? 2 : 1 ) ? 3 : 4", new BigDecimal( 3 ) );
//		test( "0 ? 2 : 1 ? 3 : 4", new BigDecimal( 3 ) );
//		test( "0 ? 2 : 0 ? 3 : 4", new BigDecimal( 4 ) );
//		test( "1 ? 2 : 3 + 4 ? 5 : 6", new BigDecimal( 2 ) );
//		test( "( 1 ? 2 : 3 ) + 4 ? 5 : 6", new BigDecimal( 5 ) );
//		test( "1 ? 2 : 3 + ( 4 ? 5 : 6 )", new BigDecimal( 2 ) );
//		test( "( 1 ? 2 : 3 ) + ( 4 ? 5 : 6 )", new BigDecimal( 7 ) );
//		test( "0 ? 2 : 3 + 4 ? 5 : 6", new BigDecimal( 5 ) );
//		test( "0 ? 2 : 3 + ( 4 ? 5 : 6 )", new BigDecimal( 8 ) );
//
//		test( "1 + 1 == 2 ? 2 : 3", new BigDecimal( 2 ) );
//		test( "!0 ? 2 : 3", new BigDecimal( 2 ) );
//	}

	@Test
	static public void test4()
	{
		test( "if( 1; 2; 3 + 1 )", new BigDecimal( 2 ) );
		test( "if( !\"\"; 2; 3 )", new BigDecimal( 2 ) );
		test( "if( !\"x\"; 2; 3 )", new BigDecimal( 3 ) );
		test( "if( 1; 2 )", new BigDecimal( 2 ) );
		test( "if( 1;; 2 )", null );
		test( "if( null; 2 )", null );
		test( "if( null;; 2 )", new BigDecimal( 2 ) );
		test( "1 || 2", new BigDecimal( 1 ) );
		test( "0 || 2", new BigDecimal( 0 ) );
		test( "null || 2", new BigDecimal( 2 ) );
		test( "1 && 2", new BigDecimal( 2 ) );
		test( "0 && 2", new BigDecimal( 2 ) );
		test( "null && 2", null );
	}

	@Test
	static public void test5()
	{
		Scope context = new Scope();

		test( "a = 1", context, new BigDecimal( 1 ) );
		Assert.assertEquals( context.get( "a" ), new BigDecimal( 1 ) );

		test( "a = b = 1", context, new BigDecimal( 1 ) );
		Assert.assertEquals( context.get( "a" ), new BigDecimal( 1 ) );
		Assert.assertEquals( context.get( "b" ), new BigDecimal( 1 ) );

		test( "1 + ( a = 1 )", context, new BigDecimal( 2 ) );
		Assert.assertEquals( context.get( "a" ), new BigDecimal( 1 ) );

		test( "1 + ( a = 1 ) + a", context, new BigDecimal( 3 ) );
		Assert.assertEquals( context.get( "a" ), new BigDecimal( 1 ) );
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
		test( "abs( +1 )", new BigDecimal( 1 ) );
		test( "abs( -1 )", new BigDecimal( 1 ) );
		test( "abs( 1 + -2 )", new BigDecimal( 1 ) );

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
		test( "defined( 1 )", true );
		test( "if( a; a )", null ); // TODO Ponder over this once more
//		test( "a && a", null ); TODO And this?
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
		test( "2; 3", new BigDecimal( 3 ) );
		test( "a = 0; b = 1", new BigDecimal( 1 ) );
		test( "a = 0;", new BigDecimal( 0 ) );
		test( ";", null );
		test( "", null );
		test( ";;; a = 0;;;; b = 1;;;", new BigDecimal( 1 ) );
		test( ";;;;", null );
	}

	@Test
	static public void test11()
	{
		test( "( 2; 3 )", new BigDecimal( 3 ) );
		test( "a = 1; a + a + a++", new BigDecimal( 3 ) );
		test( "a = 1; a + a + ++a", new BigDecimal( 4 ) );
		test( "a = 0; if( true; a++, a++ )", new BigDecimal( 1 ) );
		test( "if( a = 1, b = a, b; 3; 4 )", new BigDecimal( 3 ) );
		test( "a = 0; if( false; a++, a++; ++a, ++a )", new BigDecimal( 2 ) );
		test( "i = 0; while( i < 10; println( i++ ) )", new BigDecimal( 9 ) );
		test( "i = 0; while( i++ < 10; println( i ) )", new BigDecimal( 10 ) );
	}

	@Test
	static public void test12()
	{
		testParseFail( "println( 1 " );
		test( "f = fun( a; a * a ); f( 3 )", new BigDecimal( 9 ) );
		test( "fun( a; a * a ) ( 5 )", new BigDecimal( 25 ) );
		test( "b = 8; fun( a; a ) ( b )", new BigDecimal( 8 ) );
		test( "fun( a; a( 3 ) ) ( fun( b; 5 * b ) )", new BigDecimal( 15 ) );
		test( "fun( a, b; a( 1, 2 ) * b( 3, 4 ) ) ( fun( c, d; c * d ), fun( e, f; e * f ) )", new BigDecimal( 24 ) );
		test( "fun( a, b; a( 1, 2 ) * b( 3, 4 ) ) ( fun( a, b; a * b ), fun( a, b; a * b ) )", new BigDecimal( 24 ) );
		test( "f = fun( ; 1 ); f()", new BigDecimal( 1 ) );
		test( "a = 0; fun( ; a = 1 ) (); a", new BigDecimal( 1 ) );
		test( "fun( a; a ) ( null )", null );
		test( "f = fun( ; fun( ; 2 ) ); f()()", new BigDecimal( 2 ) );
		test( "a = 1; f = fun( ; a ); a = 2; f()", new BigDecimal( 2 ) );
		test( "fun(a;fun(;a))(1)()", new BigDecimal( 1 ) );
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
		test( "o1.test( 1 )", context, 2 );
		test( "o1.test( \"string\" )", context, 3 );
		test( "o1.test( \"string\", \"string\" )", context, 4 );
		assert o1.test( new BigDecimal( 1 ), new BigDecimal( 1 ) ) == 6;
		test( "o1.test( 1, 1 )", context, 6 );
		test( "1.getClass()", BigDecimal.class );
		test( "1.getClass()#valueOf( 1.1 )", new BigDecimal( "1.1" ) );
		test( "1.valueOf( 1.1 )", new BigDecimal( "1.1" ) );
		test( "o1.test( 1 == 1 )", context, 7 );

		TestObject2 o2 = new TestObject2();
		context.set( "o2", o2 );
		test( "o2.test( 1, 1 )", context, 1 );
	}

	@Test
	static public void test13_2()
	{
		Scope context = new Scope();
		test( "c = class( \"solidstack.script.ScriptTests$TestObject1\" );", context, TestObject1.class );
		test( "c().value", context, 0 );
		test( "c.new().value", context, 0 ); // TODO Do we want this?
		test( "c( 1 ).value", context, 2 );
		test( "c.new( 1 ).value", context, 2 );
		test( "c( \"string\" ).value", context, 3 );
		test( "c( \"string\", \"string\" ).value", context, 4 );
		test( "c( 1, 1 ).value", context, 6 );
		test( "c( 1 == 1 ).value", context, 7 );

		test( "c2 = class( \"solidstack.script.ScriptTests$TestObject2\" );", context, TestObject2.class );
		test( "c2( 1, 1 ).value", context, 1 );
	}

	@Test
	static public void test14()
	{
		test( "( a, b ) = ( 1, 2 ); a + b", new BigDecimal( 3 ) );
		test( "( a, b ) = ( 1, 2 ); a + b", new BigDecimal( 3 ) );
		test( "( a, b ) = fun( ; 1, 2 )(); a + b", new BigDecimal( 3 ) );
		test( "( a, b ) = ( fun( ; 1 ), fun( ; 2 ) ) ; a() + b()", new BigDecimal( 3 ) );
		test( "( a, b ) = ( () -> ( 1, 2 ) )(); a + b", new BigDecimal( 3 ) );
		test( "( a, b ) = ( () -> 1, () -> 2 ); a() + b()", new BigDecimal( 3 ) );
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
		test( "def( a ) = 1;", new BigDecimal( 1 ) );

		test( "fun( ; a = 1 )(); a", new BigDecimal( 1 ) ); // The function has no context of its own
		test( "a = 1; fun( a; a++ )( a ); a;", new BigDecimal( 1 ) );
		test( "a = 1; fun( ; def( a ) = 2 )(); a", new BigDecimal( 2 ) ); // The function has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", new BigDecimal( 2 ) ); // The function has no context of its own
		test( "a = 1; fun{ ; def( a ) = 2 }(); a", new BigDecimal( 1 ) ); // The function has its own context

		test( "( a = 1 ); a", new BigDecimal( 1 ) ); // The block has no context of its own
		test( "a = 1; ( def( a ) = 2 ); a", new BigDecimal( 2 ) ); // The block has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", new BigDecimal( 2 ) ); // The function has no context of its own
		test( "a = 1; { def( a ) = 2 }; a", new BigDecimal( 1 ) ); // The block has its own context

		test( "( () -> a = 1 )(); a", new BigDecimal( 1 ) ); // The function has no context of its own
		test( "a = 1; ( a -> a++ )( a ); a;", new BigDecimal( 1 ) );
		test( "a = 1; ( () -> def( a ) = 2 )(); a", new BigDecimal( 2 ) ); // The function has no context of its own
//		test( "a = 1; fun( ; val( a ) = 2 )(); a", new BigDecimal( 2 ) ); // The function has no context of its own
		test( "a = 1; f = () -> { def( a ) = 2 }; f(); a", new BigDecimal( 1 ) ); // The function has its own context
	}

	@Test
	static public void test17()
	{
		test( "class( \"java.util.ArrayList\" );", ArrayList.class );
		test( "c = class( \"java.util.ArrayList\" ); c();", new ArrayList<Object>() );
		test( "l = class( \"java.util.ArrayList\" )(); l.add( \"sinterklaas\" ); l.toArray();", new Object[] { "sinterklaas" } );
		test( "ArrayList = class( \"java.util.ArrayList\" ); l = ArrayList(); l.toArray();", new Object[ 0 ] );
	}

	@Test
	static public void test18()
	{
		test( "l = class( \"java.util.ArrayList\" )(); forEach( l, fun( i; i ) )", null );
		test( "l = class( \"java.util.ArrayList\" )(); i = 0; while( i < 10; l.add( i ), i++ ); l.each( fun( i; println( i ) ) )", new BigDecimal( 9 ) );
		eval( "Calendar = class( \"java.util.Calendar\" ); println( Calendar#getInstance().getClass() )" );
		test( "Calendar = class( \"java.util.Calendar\" ); Calendar#SATURDAY", 7 );
		fail( "Calendar = class( \"java.util.Calendar\" ); Calendar#clear()", MissingMethodException.class, "static java.util.Calendar.clear()" );
		fail( "TestObject = class( \"solidstack.script.ScriptTests$TestObject2\" ); TestObject#value", MissingFieldException.class, "static solidstack.script.ScriptTests$TestObject2.value" );
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
		ClassLoader loader = ScriptTests.class.getClassLoader();
		Assert.assertEquals( Java.forName( "java.lang.Object", loader ), Object.class );
		Assert.assertEquals( Java.forName( "java.lang.Object[]", loader ), Object[].class );
		Assert.assertEquals( Java.forName( "java.lang.Object[][]", loader ), Object[][].class );
		Assert.assertEquals( Java.forName( "int", loader ), int.class );
		Assert.assertEquals( Java.forName( "int[]", loader ), int[].class );
		Assert.assertEquals( Java.forName( "int[][][][]", loader ), int[][][][].class );
		Assert.assertEquals( Java.forName( "int[][]", loader ), int[][].class );

		test( "list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]; list[ 3 ]", new BigDecimal( 4 ) );
		test( "list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ].toArray(); list[ 3 ]", new BigDecimal( 4 ) );
		test( "list = []; list.size()", 0 );
		test( "list = class( \"java.lang.reflect.Array\" )#newInstance( class( \"int\" ), 10 ); list.size()", 10 );
		test( "map = [ 0: 1, 1: 2, 2: 3, 3: 4 ]; map[ 3 ]", new BigDecimal( 4 ) );
		test( "map = [ \"first\": 1, \"second\": 2, \"third\": 3 ]; map[ \"second\" ]", new BigDecimal( 2 ) );
		test( "map = [ \"fir\" + \"st\": 1, \"second\": 2, \"third\": 3 ]; map[ \"first\" ]", new BigDecimal( 1 ) );
		test( "map = [ \"first\": 1, \"second\": 2, \"third\": 3 ]; map[ \"fourth\" ]", null ); // TODO Undefined? Then we assign to it too.
		test( "map = [:]; map[ \"fourth\" ]", null );
		test( "map = [:]; map.size()", 0 );

		eval( "fun( a; a )( null )" );
		eval( "fun( a; a )( if( false; 1 ) )" );
		eval( "fun( a; a )( while( false; 1 ) )" );
		eval( "fun( a; a )( [].each( fun( a; () ) ) )" );

		eval( "( a -> a )( null )" );
		eval( "( a -> a )( if( false; 1 ) )" );
		eval( "( a -> a )( while( false; 1 ) )" );
		eval( "( a -> a )( [].each( a -> () ) )" );
	}

	@Test
	static public void test21()
	{
		test( "f = a -> a; f( 3 )", new BigDecimal( 3 ) );
		test( "f = a -> a * a; f( 3 )", new BigDecimal( 9 ) );
		test( "f = ( a ) -> ( a * a ); f( 3 )", new BigDecimal( 9 ) );
		test( "( a -> a * a )( 5 )", new BigDecimal( 25 ) );
		test( "( a -> a( 3 ) ) ( b -> 5 * b )", new BigDecimal( 15 ) );
		test( "( ( a, b ) -> a( 1, 2 ) * b( 3, 4 ) ) ( ( c, d ) -> c * d, ( e, f ) -> e * f )", new BigDecimal( 24 ) );
		test( "( ( a, b ) -> a( 1, 2 ) * b( 3, 4 ) ) ( ( a, b ) -> a * b, ( a, b ) -> a * b )", new BigDecimal( 24 ) );
		test( "f = () -> 1; f()", new BigDecimal( 1 ) );
		test( "a = 0; ( () -> a = 1 ) (); a", new BigDecimal( 1 ) );
		test( "( a -> a ) ( null )", null );
		test( "f = () -> () -> 2; f()()", new BigDecimal( 2 ) );
		test( "a = 1; f = () -> a; a = 2; f()", new BigDecimal( 2 ) );
		test( "( a -> () -> a )( 1 )()", new BigDecimal( 1 ) );

		test( "f = (a,b)->a*b; g = a->f(a,3); g(5)", new BigDecimal( 15 ) );
		test( "s = \"string\"; c = x->s.charAt(x); c(2)", 'r' );
		test( "f = () -> (1,2,3); (a,b,c) = f(); a+b+c;", new BigDecimal( 6 ) );

		test( "l = [ 1, 2, 3 ]; l.each( i -> println( i ) )", new BigDecimal( 3 ) );
	}

	@Test
	static public void test22()
	{
		test( "f = (a,b,c) -> a+b+c; g = (*a) -> f(*a); g(1,2,3)", new BigDecimal( 6 ) );
		test( "f = *a -> \"sinterklaas\".charAt( *a ); f( 1 )", 'i' );
		test( "f = *i -> class( \"java.util.Arrays\" )#asList( *i ); f( 1, 2, 3 ).size()", 3 );
		test( "f = ( a, *b ) -> b.size(); f( 1, 2, 3 )", 2 );
		test( "f = ( a, *b ) -> a; g = ( a, *b ) -> f( *b, a ); g( 1, 2, 3 )", new BigDecimal( 2 ) );
		test( "f = *a -> a.size(); f( 1, 2, 3 );", 3 );
		test( "l = [1,2,3]; f = (a,b,c) -> a+b+c; f(*l);", new BigDecimal( 6 ) );

		test( "( a, b, c ) = *[ 1, 2, 3 ]; a + b + c", new BigDecimal( 6 ) );
		test( "a = [ 1, 2, 3 ]; ( b, c, d ) = *a; b + c + d", new BigDecimal( 6 ) );
		test( "( 1, 2, 3 ).list().size()", 3 );
//		test( "*a = ( 1, 2, 3 ); a.size()", 3 ); // TODO
//		test( "( a, *b ) = ( 1, 2, 3 )", 3 ); TODO
//		test( "( a, *b ) = ( *[ 1, 2 ], 3 )", 3 ); TODO
		test( "a = [ 1, [ 2, 3, 4 ], 5 ]; ( (a,b,c) -> a+b+c )( *a[ 1 ] )", new BigDecimal( 9 ) );

		fail( "f = a -> (); f( 1, 2, 3 );", ScriptException.class, "Too many parameters" );
		fail( "a = *[ 1, 2, 3 ]; ( b, c, d ) = a; b + c + d", ScriptException.class, "Tuples can't be assigned to variables" );
		fail( "a = ( 1, 2, 3 ); ( b, c, d ) = a; b + c + d", ScriptException.class, "Tuples can't be assigned to variables" );

		// TODO Key value tuples for named parameters?
	}

	@Test
	static public void test23()
	{
		test( "f = (a,b,c) -> a+b+c; f( a: 1, b: 2, c: 3 )", new BigDecimal( 6 ) );
	}

	@Test
	static public void test24()
	{
		test( "s = :symbol; s.toString()", "symbol" );
		test( "s = :\"dit is ook een symbol\"; s.toString()", "dit is ook een symbol" );
		test( "s = :red; if( s == :red; true; false )", true );
	}

	// TODO Calls with named parameters
	// TODO A function without parameters, does not need the FunctionObject. Its just an unevaluated expression.
	// TODO Exceptions, catch & finally
	// TODO MethodMissing
	// TODO Default parameter values
	// TODO def & val
	// TODO Store tuples in variables?
	// TODO Binary and hexadecimal literals
	// TODO Add methods to the datatypes and/or objects
	// TODO DSLs
	// TODO Underscores in number literals
	// TODO Spread parameters or collection access
	// TODO Arrays and maps with literals
	// TODO Ranges
	// TODO Synchronization
	// TODO Return, switch, break, continue
	// TODO Threads & sleep, etc
	// TODO Assert with lazy evaluation of its arguments
	// TODO Optional? Lazy evaluation of all arguments
	// TODO // Comments, /* comments, /** comments which can contain /* comments
	// TODO Compile time (post processing) transformation functions, for example: removeMargins()
	// TODO Token interceptors that work on the token stream, or custom script parsers for eval
	// TODO Symbols :red
	// TODO Mixins
	// TODO Lazy evaluation
	// TODO Class extension pluggable
	// TODO Extensions: unique/each(WithIndex)/find(All)/collect/contains/every/indexOf/flatten/groupBy/inject/join/max/min/removeAll/replaceAll/reverse/sum/tail/traverse/withReader(etc)
	// TODO with() to execute a function with a different context
	// TODO Currying

	static public void test( String expression, Object expected )
	{
		test( expression, null, expected );
	}

	static public void test( String expression, Scope context, Object expected )
	{
		Object result = eval( expression, context );
		Assert.assertEquals( result, expected );
	}

	static public Object eval( String expression )
	{
		return eval( expression, null );
	}

	static public Object eval( String expression, Scope context )
	{
		Script script = Script.compile( expression );
//		String dump = new Dumper().dump( script );
		return script.execute( context );
	}

	static public void testParseFail( String expression )
	{
		try
		{
			Script.compile( expression );
			Assert.fail( "Expected a SourceException" );
		}
		catch( SourceException e )
		{
			// Expected
		}
	}

	static public void fail( String expression, Class<? extends Exception> exception, String message )
	{
		try
		{
			eval( expression );
			failBecauseExceptionWasNotThrown( exception );
		}
		catch( Exception t )
		{
			assertThat( t ).isExactlyInstanceOf( exception );
			assertThat( t ).hasMessageContaining( message );
		}
	}

//	static private void test( String file ) throws IOException
//	{
//		InputStream in = ScriptTests.class.getResourceAsStream( file );
//		Reader reader = new InputStreamReader( in );
//		char[] buffer = new char[ 1024 ];
//		StringBuilder contents = new StringBuilder();
//		int len;
//		while( ( len = reader.read( buffer ) ) >= 0 )
//			contents.append( buffer, 0, len );
//		Script.compile( contents.toString() ).execute( null );
//		// TODO Validate result
//	}

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

		public int test() { return 0; }
		public int test( int i ) { return 1; }
		public int test( BigDecimal i ) { return 2; }
		public int test( String s ) { return 3; }
		public int test( String... s ) { return 4; }
		public int test( BigDecimal... b ) { return 5; }
		public int test( BigDecimal b1, Number b2 ) { return 6; }
		public int test( boolean b ) { return 7; }
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
