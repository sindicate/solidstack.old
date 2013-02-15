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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.SourceReader;
import solidstack.io.memfs.Folder;
import solidstack.io.memfs.Resource;
import solidstack.script.java.Java;
import solidstack.script.objects.PString;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.GlobalScope;
import solidstack.script.scopes.Scope;
import funny.Symbol;


@SuppressWarnings( { "javadoc", "unchecked", "rawtypes" } )
public class ScriptTests extends Util
{
	@Test
	static public void helloWorld()
	{
		test( "println( \"Hello World!\" )", "Hello World!" );
		DefaultScope scope = new DefaultScope();
		scope.set( Symbol.apply( "var1" ), "Value" );
		test( "var1", scope, "Value" );
		test( "", null );
	}

	@Test
	static public void test2()
	{
		DefaultScope scope = new DefaultScope();
		scope.set( Symbol.apply( "var1" ), 1 );
		test( "var1 + 1", scope, 2 );
	}

	@Test
	static public void test3()
	{
		test( "1 + 1 * 2", \u0033 ); // Unicode 33 is '3'
		test( "2 * 1 + \u0031", 3 ); // Unicode done by Java parser
//		test( "2 * 1 + \\u0031", 3 ); // Unicode done by script parser
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
		DefaultScope scope = new DefaultScope();

		test( "a = 1", scope, 1 );
		Assert.assertEquals( scope.get( Symbol.apply( "a" ) ), 1 );

		test( "a = b = 1", scope, 1 );
		Assert.assertEquals( scope.get( Symbol.apply( "a" ) ), 1 );
		Assert.assertEquals( scope.get( Symbol.apply( "b" ) ), 1 );

		test( "1 + ( a = 1 )", scope, 2 );
		Assert.assertEquals( scope.get( Symbol.apply( "a" ) ), 1 );

		test( "1 + ( a = 1 ) + a", scope, 3 );
		Assert.assertEquals( scope.get( Symbol.apply( "a" ) ), 1 );
	}

	@Test
	static public void test6()
	{
		test( "\"test\"", "test" );
		test( "\"test\" + \"test\"", "testtest" );
		test( "\"\u00E9\"", "é" ); // Unicode escape parsed by Java
		test( "\"\\u00E9\"", "é" ); // Unicode escape parsed by script
	}

	@Test
	static public void test7()
	{
		test( "(+1).abs()", 1 );
		test( "(-1).abs()", 1 );
		test( "-1.abs()", -1 );
		test( "(1+ -2).abs()", 1 );

		test( "\"sinterklaas\".substring( 1+2*1, 9-1-1 )", "terk" );
		test( "\"sinterklaas\".substring( 6 )", "klaas" );
		test( "\"sinterklaas\".toUpperCase()", "SINTERKLAAS" );
		test( "println( \"Hello World!\" )", "Hello World!" );
		test( "println( \"Hello World!\".toUpperCase() )", "HELLO WORLD!" );
		test( "\"sinterklaas\".length()", 11 );
		test( "\"sinterklaas\".size()", 11 );
		test( "defined( a )", false ); // TODO or a.isDefined() ?
		test( "defined( var a )", true );
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
//		test( "a = 1; a + a + a++", 3 );
//		test( "a = 1; a + a + ++a", 4 );
		// TODO If should stop at the comma just as with the ;
		test( "a = 0; ( b, c ) = if( true ) ( a = a + 1, a = a + 1 )", Arrays.asList( 1, 2 ) );
		test( "if( a = 1, b = a, b ) 3 else 4", 3 );
		test( "if( a = null, b = a, b ) 3 else 4", 4 );
		test( "a = 0; ( b, c ) = if( false ) ( 1, 2 ) else ( 3, 4 )", Arrays.asList( 3, 4 ) );
//		test( "i = 0; while( i < 10 ) print( i++ )", 9 );
		test( "i = 0; while( i < 10 ) ( print( i ); i = i + 1 )", 10 );
//		test( "i = 0; while( i < 10 && print( i = i + 1 ) ) ()", null ); // TODO Is there an example where result of condition should be returned?
	}

	@Test
	static public void test13()
	{
		DefaultScope scope = new DefaultScope();
		scope.set( Symbol.apply( "s" ), "sinterklaas" );
		test( "s.length()", scope, 11 );
		test( "s.substring( 6 )", scope, "klaas" );
		test( "s.substring( 1, 6 )", scope, "inter" );
		test( "s.contains( \"kl\" )", scope, true );

		TestObject1 o1 = new TestObject1();
		scope.set( Symbol.apply( "o1" ), o1 );
		test( "o1.test()", scope, 0 );
		test( "o1.test( 1.0 )", scope, 2 );
		test( "o1.test( \"string\" )", scope, 3 );
		test( "o1.test( \"string\", \"string\" )", scope, 4 );
		assert o1.test( new BigDecimal( 1 ), new BigDecimal( 1 ) ) == 6;
		test( "o1.test( 1, 1 )", scope, 6 );
		test( "1.getClass()", Integer.class );
		test( "1.1.getClass()", BigDecimal.class );
		test( "loadClass( 1.0.getClass() ).valueOf( 1.1 as double )", new BigDecimal( "1.1" ) );
		test( "1.00.valueOf( 1.1 as double )", new BigDecimal( "1.1" ) );
		test( "o1.test( 1 == 1 )", scope, 7 );
//		test( "o1.test( a = 1, b = 2 )", scope, 8 ); TODO

		TestObject2 o2 = new TestObject2();
		scope.set( Symbol.apply( "o2" ), o2 );
		test( "o2.test( 1, 1 )", scope, 1 );
	}

	@Test
	static public void test13_2()
	{
		DefaultScope scope = new DefaultScope();
		test( "c = loadClass( \"solidstack.script.ScriptTests$TestObject1\" );", scope, TestObject1.class );
		test( "new c().value", scope, 0 );
		test( "new c( 3.14 ).value", scope, 2 );
		test( "new c( 0.123E-10 ).value", scope, 2 );
		test( "new c( \"string\" ).value", scope, 3 );
		test( "new c( \"string\", \"string\" ).value", scope, 4 );
		test( "new c( 1, 1 ).value", scope, 6 );
		test( "new c( 1 == 1 ).value", scope, 7 );
//		test( "new c( a = 1, b = 2 ).value", scope, 8 ); TODO

		test( "c2 = loadClass( \"solidstack.script.ScriptTests$TestObject2\" );", scope, TestObject2.class );
		test( "new c2( 1, 1 ).value", scope, 1 );
	}

	@Test
	static public void test14()
	{
		test( "( a, b ) = ( 1, 2 ); a + b", 3 );
		test( "( a, b ) = ( 1, 2 ); a + b", 3 );
		test( "( a, b ) = ( () => ( 1, 2 ) )(); a + b", 3 );
		test( "( a, b ) = ( () => 1, () => 2 ); a() + b()", 3 );
	}

	@Test
	static public void pStrings()
	{
		test( "a = 1; s\"a = ${a}\".toString()", "a = 1" );
		test( "a = 1; s = s\"a = ${a}\"; a = 2; s.toString()", "a = 1" );
		test( "a = 1; s\"a = \\${a}\"", "a = ${a}" );
		test( "s\"${1}\".toString()", "1" );
		test( "s\"${}\"", "" );
		test( "s\"\".getClass()", String.class );
		test( "s\"x\".getClass()", String.class );
		test( "s\"${1}\".getClass()", PString.class );
		test( "s\"x${1}x\".getClass()", PString.class );
		test( "s\"x${1}x\".size()", 3 );
		test( "s\"${\"x\"}\".toString()", "x" );
		failParse( "\"${\"x\"}\"", "Unexpected token 'x'" );
		test( "s\"\\\"${1}\\\"\".toString()", "\"1\"" );
		test( "s\"\\\"${\"X\"}\\\"\".toString()", "\"X\"" );
	}

	@Test
	static public void nestedScopes()
	{
		test( "var a = 1;", 1 );

		test( "( a = 1 ); a", 1 ); // The block has no scope of its own
		test( "a = 1; ( var a = 2 ); a", 2 ); // The block has no scope of its own
		test( "a = 1; { var a = 2 }; a", 1 ); // The block has its own scope

		test( "( () => a = 1 )(); a", 1 ); // The function has no scope of its own
		test( "a = 1; ( a => a = 2 )( a ); a;", 1 );
		test( "a = 1; ( () => var a = 2 )(); a", 2 ); // The function has no scope of its own
		test( "a = 1; f = () => { var a = 2 }; f(); a", 1 ); // The function has its own scope
	}

	@Test
	static public void test17()
	{
		test( "loadClass( \"java.util.ArrayList\" );", ArrayList.class );
		test( "new ArrayList();", new ArrayList<Object>() );
		test( "l = new ArrayList(); l.add( \"sinterklaas\" ); l.toArray();", new Object[] { "sinterklaas" } );
		test( "l = new ArrayList(); l.toArray();", new Object[ 0 ] );
	}

	@Test
	static public void test18()
	{
		test( "l = new ArrayList(); i = 0; while( i < 10 ) ( l.add( i ); i = i + 1 ); l.foreach( i => println( i ) )", 9 );
		eval( "println( Calendar.getInstance().getClass() )" );
		test( "Calendar.SATURDAY", 7 );
		fail( "Calendar.clear()", ScriptException.class, "static java.util.Calendar.clear()" );
		fail( "TestObject = loadClass( \"solidstack.script.ScriptTests$TestObject2\" ); TestObject.value", ScriptException.class, "static solidstack.script.ScriptTests$TestObject2.value" );
	}

	@Test
	static public void test19()
	{
		fail( "o = new ( loadClass( \"solidstack.script.ScriptTests$TestObject3\" ) )(); o.throwException()", ScriptException.class, "test exception" );
	}

	@Test
	static public void collections() throws ClassNotFoundException
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

		// TODO Add Array
		test( "list = List( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ); list( 3 )", 4 );
		test( "list = LinkedList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ); list( 3 )", 4 );
		test( "set = Set( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ); set.contains( 3 )", true );
		test( "array = List( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ).toArray(); array( 3 )", 4 );
		test( "list = List(); list.size()", 0 );

		test( "map = Map( 0 -> 1, 1 -> 2, 2 -> 3, 3 -> 4 ); map( 3 )", 4 );
		test( "map = Map( \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ); map( \"second\" )", 2 );
		test( "map = Map( \"fir\" + \"st\" -> 1, \"second\" -> 2, \"third\" -> 3 ); map( \"first\" )", 1 );
		test( "map = Map( \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ); map( \"fourth\" )", null ); // TODO Undefined? Then we assign to it too.
		test( "map = Map( \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ); map.third", 3 );
		test( "map = Map( \"first\" -> 1, \"second\" -> 2, \"third\" -> 3 ); map.fourth", null ); // TODO What about undefined?
		test( "map = Map(); map.first = 1; map( \"first\" )", 1 );
		test( "map = Map(); map( \"fourth\" )", null );
		test( "map = Map(); map.size()", 0 );
		test( "map = Map(); map( \"third\" ) = 3; map( \"third\" )", 3 );
		test( "map = LinkedHashMap( 0 -> 1 ); map.size()", 1 );

		test( "set = Set( 0 ); set.size()", 1 );
		test( "set = LinkedHashSet( 0, 1, 0 ); set.size()", 2 );

		test( "props = Properties( \"prop1\" -> \"value1\" ); props.prop1", "value1" );

		test( "array = loadClass( \"java.lang.reflect.Array\" ).newInstance( loadClass( \"java.lang.String\" ), 10 ); array.size()", 10 );
		test( "array = loadClass( \"java.lang.reflect.Array\" ).newInstance( loadClass( \"int\" ), 10 ); array.size()", 10 );

		test( "scope = Scope( \"test\" -> 1, 'test2 -> 2 ); scope.test", 1 );
		test( "scope = Scope(); with( scope )( test = 1 ); scope.test", 1 );
		test( "scope = Scope(); compile( \"test = 1\" ).eval( scope ); scope.test", 1 );
//		test( "scope = Scope(); x = =>( test = 1 ); x.eval( scope ); scope.test", 1 );
//		test( "scope = Scope(); scope.do( test = 1 ); scope.test", 1 );

		eval( "( a => a )( null )" );
		eval( "( a => a )( if( false; 1 ) )" );
		eval( "( a => a )( while( false ) 1 )" );
		eval( "( a => a )( List().foreach( a => () ) )" );
	}

	@Test
	static public void toStringTests()
	{
		test( "List().toString()", "[]" ); // JDK
		test( "Map().toString()", "{}" ); // JDK
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

		test( "l = List( 1, 2, 3 ); l.foreach( i => println( i ) )", 3 );
	}

	@Test
	static public void test22()
	{
		test( "f = (a,b,c) => a+b+c; g = (*a) => f(*a); g(1,2,3)", 6 );
		test( "f = *a => \"sinterklaas\".charAt( *a ); f( 1 )", 'i' );
		test( "asList = *i => Arrays.asList( *i ); list = asList( 1, 2, 3 ); list.size()", 3 );
		test( "f = ( a, *b ) => b.size(); f( 1, 2, 3 )", 2 );
		test( "f = ( a, *b ) => a; g = ( a, *b ) => f( *b, a ); g( 1, 2, 3 )", 2 );
		test( "f = *a => a.size(); f( 1, 2, 3 );", 3 );
		test( "l = List(1,2,3); f = (a,b,c) => a+b+c; f(*l);", 6 );

		test( "( a, b, c ) = *List( 1, 2, 3 ); a + b + c", 6 );
		test( "a = List( 1, 2, 3 ); ( b, c, d ) = *a; b + c + d", 6 );
		test( "( 1, 2, 3 ).list().size()", 3 );
//		test( "*a = ( 1, 2, 3 ); a.size()", 3 ); // TODO
//		test( "( a, *b ) = ( 1, 2, 3 )", 3 ); TODO
//		test( "( a, *b ) = ( *[ 1, 2 ], 3 )", 3 ); TODO
		test( "a = List( 1, List( 2, 3, 4 ), 5 ); ( (a,b,c) => a+b+c )( *a( 1 ) )", 9 );

		fail( "f = a => (); f( 1, 2, 3 );", ScriptException.class, "Too many parameters" );
		fail( "a = *List( 1, 2, 3 ); ( b, c, d ) = a; b + c + d", ScriptException.class, "Can't assign tuples to variables" );
		fail( "a = ( 1, 2, 3 ); ( b, c, d ) = a; b + c + d", ScriptException.class, "Can't assign tuples to variables" );

		// TODO Key value tuples for named parameters?
	}

	@Test
	static public void testNamedParameters()
	{
		// TODO Calling default global methods with named parameter
		test( "f = (a,b,c) => a; f( b = 1, c = 2, a = 3 )", 3 );
		test( "f = (a,b=0,c=0) => a; f( a = 3 )", 3 );
		test( "f = (a,b=0,c=0) => a; f( a = null )", null );
		test( "f = (a=0,b,c=0) => a; f( b = 1 )", 0 );
		test( "f = () => (); f()", null );
		test( "f = (a=1) => a; f()", 1 );
		test( "f = (a=1) => a; f(2)", 2 );
		fail( "f = (a=1) => a; f(2,3)", ScriptException.class, "Too many parameters" );
		test( "f = (a=1,b=2) => a+b; f()", 3 );
		test( "f = (a=1,b=2) => a+b; f(3)", 5 );
		test( "f = (a=1,b=2) => a+b; f(3,4)", 7 ); // TODO _ as placeholder
		fail( "f = (a=1,b=2) => a+b; f(3,4,5)", ScriptException.class, "Too many parameters" );
		test( "f = (a=1,b=2) => a+b; f(a=3)", 5 );
		test( "f = (a=1,b=2) => a+b; f(b=4)", 5 );
		test( "f = (a=1,b=2) => a+b; f(b=4,a=3)", 7 );
		test( "x=5; f = (a=x,b=2) => a+b; { var x=7; f() }", 7 );
//		test( "f = ( =>a, =>b ) => a; f(b=4,a=3)", 7 );
	}

//	@Test
//	static public void testDef()
//	{
//		test( "def f(a) = a; f(1)", 1 );
//	}

	@Test
	static public void test24()
	{
		Symbol real1 = Symbol.apply( "symbol" );
		Symbol real2 = Symbol.apply( "symbol" );
//		Symbol temp1 = new TempSymbol( "symbol" );
//		Symbol temp2 = new TempSymbol( "symbol" );
		assertThat( real1 ).isSameAs( real2 );
		assertThat( real1 ).isEqualTo( real2 );
//		assertThat( real1 ).isEqualTo( temp1 );
//		assertThat( temp1 ).isEqualTo( real1 );
//		assertThat( temp1 ).isEqualTo( temp2 );

		test( "s = 'symbol; s.toString()", "symbol" );
		test( "s = 'red; if( s == 'red ) true else false", true );
		test( "s = Symbol( \"dit is ook een symbol\" ); s.toString()", "dit is ook een symbol" );
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
		test( "List() as boolean", false );
		test( "List(1) as boolean", true );
		test( "Map() as boolean", false );
		test( "Map(1->1) as boolean", true );

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
		test( "\"1\" instanceof CharSequence", true );
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
		test( "-3%2", -1 );

		test( "16 as float / 15 as byte", (float)16 / 15 );
		test( "16 as float % 15 as byte", (float)1 );

		test( "-15 as byte", (byte)-15 );
		test( "-15 as char", (char)-15 );
		test( "-(15 as byte)", -15 );
		test( "-(15 as char)", -15 );
		test( "-(1.5 as float)", -1.5f );

		test( "(-15as byte).abs()", 15 );
		test( "( -15 as char ).abs()", 65521 );
		test( "( -15 ).abs()", 15 );
		test( "( -1.5 as float ).abs()", 1.5f );

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
		DefaultScope scope = new DefaultScope();
		scope.set( Symbol.apply( "o1" ), new TestObject1() );

		fail( "1 = 1", ScriptException.class, "Can't assign to a java.lang.Integer" );
		fail( "loadClass( \"xxx\" )", ScriptException.class, "Class not found: xxx" );
		fail( "1.xxx", ScriptException.class, "No such field: java.lang.Integer.xxx" );
		fail( "Integer.xxx", ScriptException.class, "No such field: static java.lang.Integer.xxx" );
		fail( "o1.test( null )", scope, ScriptException.class, "test(java.util.Map)" );
		fail( "f = ( *b, c ) => (); f()", ScriptException.class, "Collecting parameter must be the last parameter" );
		fail( "f = ( a ) => (); f()", ScriptException.class, "Not enough parameters" );
		fail( "f = () => (); f( 1 )", ScriptException.class, "Too many parameters" );
		fail( "f()", ScriptException.class, "'f' undefined" );
		fail( "f = null; f()", ScriptException.class, "Function is null" );
		fail( "f = 1; f()", ScriptException.class, "No such method: java.lang.Integer.apply()" );
		fail( "a = ( 1, 2 )", ScriptException.class, "Can't assign tuples to variables" );
//		fail( "f = null; f[]", ScriptException.class, "Null can't be indexed" );
//		fail( "f = 1; f[]", ScriptException.class, "Missing index" );
//		fail( "f = 1; f[ 1 ]", ScriptException.class, "Can't index a java.lang.Integer" );
//		fail( "--1", ScriptException.class, "Can't apply -- to a java.lang.Integer" );
//		fail( "++1", ScriptException.class, "Can't apply ++ to a java.lang.Integer" );
//		fail( "1--", ScriptException.class, "Can't apply -- to a java.lang.Integer" );
//		fail( "1++", ScriptException.class, "Can't apply ++ to a java.lang.Integer" );
//		fail( "--null", ScriptException.class, "Can't apply -- to a null" );
//		fail( "++null", ScriptException.class, "Can't apply ++ to a null" );
//		fail( "null--", ScriptException.class, "Can't apply -- to a null" );
//		fail( "null++", ScriptException.class, "Can't apply ++ to a null" );
		fail( "Map( a -> 2 )", ScriptException.class, "'a' undefined" );
		fail( "Map( 1 -> 2, 3 )", ScriptException.class, "No such method: static java.util.Map.apply() is applicable" );
		failParse( "'1", "Unexpected character" );
		failParse( "var", "identifier expected after 'var', not EOF, at line 1" );
		failParse( "var 1", "identifier expected after 'var', not 1" );
//		fail( "defined()", ScriptException.class, "defined() needs exactly one parameter" ); TODO
//		fail( "defined( 1 )", ScriptException.class, "defined() needs a variable identifier as parameter" ); TODO?
		fail( "print()", ScriptException.class, "'print' undefined" );
		fail( "println()", ScriptException.class, "'println' undefined" );
//		fail( "scope()", ScriptException.class, "scope() needs exactly one parameter" );
//		fail( "scope( 1 )", ScriptException.class, "scope() needs a map parameter" );
		failParse( "throw", "expression expected after 'throw'" );
		failParse( "throw;", "expression expected after 'throw'" );
		fail( "throw()", ScriptException.class, "null" );
//		fail( "val()", ScriptException.class, "val() needs exactly one parameter" );
//		fail( "val( 1 )", ScriptException.class, "val() needs a variable identifier as parameter" );
		fail( "f = ( a = 1 ) => (); f( b = 1 )", ScriptException.class, "Parameter 'b' undefined" );
		fail( "f = ( a ) => (); f( b = 1 )", ScriptException.class, "No value specified for parameter 'a'" );
		fail( "f = ( a, b ) => (); f( a = 1, 2 )", ScriptException.class, "All parameters must be named" );
		fail( "f = ( a, b ) => (); f( 1, b = 2 )", ScriptException.class, "All parameters must be named" );
		fail( "f = ( a ) => (); f( \"a\" = 1 )", ScriptException.class, "Parameter must be named with a variable identifier" );
	}

//	@Test TODO
//	static public void test28()
//	{
//		test( "if( true ) return( true ); return( false )", true );
//		test( "l = List( 1, 2, 3 ); l.foreach( i => return( i ) ); 4", 1 );
//		test( "l = List( 1, 2, 3 ); f = i => return( i ); l.foreach( f ); 4", 4 );
//	}

	@Test
	static public void test29()
	{
		test( "List( 1, 2, 3 ).mkString( \"; \" )", "1; 2; 3" );
		test( "List( 1, 2, 3 ).mkString( \"List( \", \", \", \" )\" )", "List( 1, 2, 3 )" );
		test( "List( 1, 2, 3 ).iterator().mkString( \"; \" )", "1; 2; 3" );
		test( "List( 1, 2, 3 ).iterator().mkString( \"List( \", \", \", \" )\" )", "List( 1, 2, 3 )" );
		test( "Array.newInstance( String, 3 ).mkString()", "nullnullnull" );

		test( "List( 1, 2, 3 ).filter( n => n == 2 )", Arrays.asList( 2 ) );
		test( "List( 1, 2, 3 ).filter( n => () )", Arrays.asList() );
		test( "List( 1, 2, 3 ).map( n => s\"${n}.0\".toString() )", Arrays.asList( "1.0", "2.0", "3.0" ) );
		test( "List( 1, 2, 3 ).fold( 0, ( a, b ) => a + b )", 6 );
	}

	@Test
	static public void test30()
	{
		Object scope = new TestObject4();
		test( "field1", scope, 1 );
		test( "field1 = 2; field2", scope, 2 );
		test( "field2", scope, 2 );
		test( "field2 = 3; field2", scope, 3 );
		test( "getField2()", scope, 3 );
		test( "setField2( 4 ); field2", scope, 4 );

		Scope scope2 = new DefaultScope();
		scope2.var( Symbol.apply( "obj" ), new TestObject4() );
		test( "with( obj ) field1", scope2, 1 );
		test( "with( obj ) ( field1 = 2; field2 )", scope2, 2 );
		test( "with( obj ) field2", scope2, 2 );
		test( "with( obj ) ( field2 = 3; field2 )", scope2, 3 );
		test( "with( obj ) getField2()", scope2, 3 );
		test( "with( obj ) ( setField2( 4 ); field2 )", scope2, 4 );
	}

	@Test
	static public void test31()
	{
		Object object = new TestObject4();
		DefaultScope scope = new DefaultScope();
		scope.var( Symbol.apply( "o" ), object );
		test( "o.field1", scope, 1 );
		test( "o.field1 = 2; o.field2", scope, 2 );
		test( "o.field2", scope, 2 );
		test( "o.field2 = 3; o.field2", scope, 3 );
		test( "o.getField2()", scope, 3 );
		test( "o.setField2( 4 ); o.field2", scope, 4 );
	}

	@Test
	static public void test32()
	{
		Map scope = new HashMap();
		scope.put( "field1", 1 );
		scope.put( "field2", 2 );
		test( "field1", scope, 1 );
		test( "field1 = 2; field2", scope, 2 );
		test( "field2", scope, 2 );
		test( "field2 = 3; field2", scope, 3 );
	}

	@Test
	static public void test33()
	{
		Map map = new HashMap();
		map.put( "field1", 1 );
		map.put( "field2", 2 );
		DefaultScope scope = new DefaultScope();
		scope.var( Symbol.apply( "o" ), map );
		test( "o.field1", scope, 1 );
		test( "o.field1 = 2; o.field2", scope, 2 );
		test( "o.field2", scope, 2 );
		test( "o.field2 = 3; o.field2", scope, 3 );
	}

	@Test
	static public void modules() throws FileNotFoundException
	{
		eval( "module( \"m1\" )( m2 = 3 )" );
		test( "m1.m2", 3 );

		Folder root = new Folder();
		Resource module = root.putFile( "module.funny", "module( \"m1\" )( m2 = 3 )" );
		root.putFile( "script.funny", "require( \"module.funny\" ); m1.m2" );

		SourceReader reader = root.getSourceReader( "script.funny" );
		Script script = load( reader );

		GlobalScope.instance.reset();
		test( script, 3 );

		// ---- Second time skips
		module.setContents( "module( \"m1\" )( throw \"Should not come here\" )" );
		script.eval();

		// TODO Require does nothing more than execute the script file
		// ---- Circular tests
		GlobalScope.instance.reset();
		root.putFile( "module.funny", "module( \"m1\" )( require( \"module2.funny\" ) )" );
		root.putFile( "module2.funny", "module( \"m2\" )( require( \"module.funny\" ) )" );
		fail( script, ScriptException.class, "Circular module dependency detected" );

		// ---- Constructor
		GlobalScope.instance.reset();
		root = new Folder();
		root.putFile( "deployer.funny", "module( \"Deployer\" )( var Deployer = x => x * 2 )" );
		root.putFile( "script.funny", "require( \"deployer.funny\" ); var deployer = Deployer.Deployer( 1 )" );
		reader = root.getSourceReader( "script.funny" );
		script = load( reader );
		test( script, 2 );
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
	// TODO Exceptions, catch & finally
	// TODO MethodMissing
	// TODO Scala dynamic?
	// DONE Default parameter values
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
	// TODO Threads, sleep, wait, notify, join, etc
	// TODO Assert with lazy evaluation of its arguments
	// TODO Optional? Lazy evaluation of all arguments
	// DONE // Comments, /* comments
	// TODO /** comments which can contain /* comments
	// TODO Compile time (post processing) transformation functions, for example: removeMargins()
	// TODO Token interceptors that work on the token stream, or custom script parsers for eval
	// DONE Symbols :red
	// TODO Mixins
	// TODO Class extension pluggable
	// TODO with() to execute a function with a different scope
	// TODO Multiple parameters lists: fun(args)(args)
	// TODO Operator calling method on first operand, operator overloading
	// TODO Hints for null parameters: a as String which evaluates to a TypedNull object if a is null
	// TODO Compilation errors including column number
	// TODO Compartmentalization like Java does with classloaders. This means name spaces too.
	// TODO Adding tuples or appending values to it.
	// TODO Always remember the lexical scope. Needed if we want script file specific settings.
	// TODO Axis: owner = lexical owner, delegate, prototype, global, this
	// TODO Add resource attribute or resource() method which returns the resource of the current script
	// TODO Reloadable scripts, need ResourceLoader (like the TemplateLoader)
	// TODO Caching of loaded and compiled scripts (to execute repeatedly)
	// TODO Modules and namespaces
	// TODO Values with extra attributes. For example: val is a String, but val.kind is something else

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

	static public class TestObject3
	{
		public void throwException() throws Exception
		{
			throw new Exception( "test exception" );
		}
	}

	static public class TestObject4
	{
		public int field1 = 1;
		private int _field2 = 2;
		public int getField2() { return this._field2; }
		public void setField2( int value ) { this._field2 = value; }
	}
}
