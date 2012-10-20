package solidstack.script;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings( "javadoc" )
public class ScriptTests
{
	@Test
	static public void test1()
	{
		Context context = new Context();
		context.set( "var1", "Value" );
		Script script = Script.compile( "var1" );
		Object result = script.execute( context );
		Assert.assertEquals( result, "Value" );
	}

	@Test
	static public void test2()
	{
		Context context = new Context();
		context.set( "var1", 1 );
		Script script = Script.compile( "var1 + 1" );
		Object result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 2 ) );
	}

	@Test
	static public void test3()
	{
		test( "1 + 1 * 2", new BigDecimal( 3 ) );
		test( "2 * 1 + 1", new BigDecimal( 3 ) );
		test( "1 + 1 + 1", new BigDecimal( 3 ) );
		test( "1 + 2 * 2 + 1", new BigDecimal( 6 ) );
		test( "( 1 + 2 ) * 2 + 1", new BigDecimal( 7 ) );
		test( "1 + 2 * ( 2 + 1 )", new BigDecimal( 7 ) );
		test( "( 1 + 2 ) * ( 2 + 1 )", new BigDecimal( 9 ) );
	}

	static private void test( String expression, Object expected )
	{
		Script script = Script.compile( expression );
		Object result = script.execute( null );
		Assert.assertEquals( result, expected );
	}

	static private void test( String expression, Context context, Object expected )
	{
		Script script = Script.compile( expression );
		Object result = script.execute( context );
		Assert.assertEquals( result, expected );
	}

	@Test
	static public void test4()
	{
		int val = 1;
		Assert.assertEquals( val > 0 ? 2 : 3 + 1, 2 );
		Assert.assertEquals( val + 1 > 0 ? 2 : 3 + 1, 2 );
		Assert.assertEquals( val > 0 ? 2 : val > 0 ? 3 : 4, 2 );

		test( "1 ? 2 : 3 + 1", new BigDecimal( 2 ) );
		test( "( 1 ? 2 : 3 ) + 1", new BigDecimal( 3 ) );
		test( "1 + 1 ? 2 : 3 + 1", new BigDecimal( 2 ) );
		test( "1 + ( 1 ? 2 : 3 ) + 1", new BigDecimal( 4 ) );
		test( "1 + ( 1 ? 2 : 3 + 1 )", new BigDecimal( 3 ) );
		test( "( 1 + 1 ? 2 : 3 ) + 1", new BigDecimal( 3 ) );
		test( "0 ? 2 : 3 + 4 * 5", new BigDecimal( 23 ) );
		test( "0 ? 2 : ( 3 + 4 ) * 5", new BigDecimal( 35 ) );
		test( "( 0 ? 2 : 3 ) + 4 * 5", new BigDecimal( 23 ) );
		test( "( ( 0 ? 2 : 3 ) + 4 ) * 5", new BigDecimal( 35 ) );
		test( "1 ? 1 ? 2 : 3 : 4", new BigDecimal( 2 ) );
		test( "1 ? 0 ? 2 : 3 : 4", new BigDecimal( 3 ) );
		test( "0 ? 0 ? 2 : 3 : 4", new BigDecimal( 4 ) );
		test( "1 ? 2 : 1 ? 3 : 4", new BigDecimal( 2 ) );
		test( "( 1 ? 2 : 1 ) ? 3 : 4", new BigDecimal( 3 ) );
		test( "0 ? 2 : 1 ? 3 : 4", new BigDecimal( 3 ) );
		test( "0 ? 2 : 0 ? 3 : 4", new BigDecimal( 4 ) );
		test( "1 ? 2 : 3 + 4 ? 5 : 6", new BigDecimal( 2 ) );
		test( "( 1 ? 2 : 3 ) + 4 ? 5 : 6", new BigDecimal( 5 ) );
		test( "1 ? 2 : 3 + ( 4 ? 5 : 6 )", new BigDecimal( 2 ) );
		test( "( 1 ? 2 : 3 ) + ( 4 ? 5 : 6 )", new BigDecimal( 7 ) );
		test( "0 ? 2 : 3 + 4 ? 5 : 6", new BigDecimal( 5 ) );
		test( "0 ? 2 : 3 + ( 4 ? 5 : 6 )", new BigDecimal( 8 ) );
	}

	@Test
	static public void test5()
	{
		Context context = new Context();

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
	}

	@Test
	static public void test8()
	{
		test( "1 == 1", true );
		test( "1 == 0", false );
		test( "1 + 1 == 2 ? 2 : 3", new BigDecimal( 2 ) );

		test( "true", true );
		test( "false", false );
		test( "1 == 1 == true", true );
		test( "true == 1 == 1", false );
		test( "true == ( 1 == 1 )", true );

		test( "!true", false );
		test( "!false", true );
		test( "!( 1 == 1 )", false );
		test( "!( 1 == 0 )", true );
		test( "!0 ? 2 : 3", new BigDecimal( 2 ) );
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
		test( "{ 2; 3 }", new BigDecimal( 3 ) );
		test( "a = 1; a + a + a++", new BigDecimal( 3 ) );
		test( "a = 1; a + a + ++a", new BigDecimal( 4 ) );
		test( "a = 0; if( true ) { a++; a++ }", new BigDecimal( 1 ) );
		test( "if( a = 1, b = a, b ) 3 else 4", new BigDecimal( 3 ) );
		test( "a = 0; if( false ) { a++; a++ } else { ++a; ++a }", new BigDecimal( 2 ) );
		test( "a = 0; while( a < 10 ) { print( ++a ) }", new BigDecimal( 10 ) );
	}

	@Test
	static public void test12()
	{
		test( "f = function( a ) { a * a }; f( 3 )", new BigDecimal( 9 ) );
		test( "function( a ) { a * a } ( 5 )", new BigDecimal( 25 ) );
		test( "b = 8; function( a ) { a } ( b )", new BigDecimal( 8 ) );
		test( "function( a ) { a( 3 ) } ( function( b ) { 5 * b } )", new BigDecimal( 15 ) );
		test( "function( a, b ) { a( 1, 2 ) * b( 3, 4 ) } ( function( c, d ) { c * d }, function( e, f ) { e * f } )", new BigDecimal( 24 ) );
		test( "function( a, b ) { a( 1, 2 ) * b( 3, 4 ) } ( function( a, b ) { a * b }, function( a, b ) { a * b } )", new BigDecimal( 24 ) );
		test( "f = function() { 1 }; f()", new BigDecimal( 1 ) );
		test( "a = 0; function() { a = 1 } (); a", new BigDecimal( 0 ) ); // TODO This may not be right
	}

	@Test
	static public void test13()
	{
		Context context = new Context();
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
		test( "1.getClass().valueOf( 1.1 )", new BigDecimal( "1.1" ) );
		test( "o1.test( 1 == 1 )", context, 7 );

		TestObject2 o2 = new TestObject2();
		context.set( "o2", o2 );
		test( "o2.test( 1, 1 )", context, 1 );
	}

	@SuppressWarnings( "unused" )
	static public class TestObject1
	{
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
		public int test( int i1, int i2 ) { return 1; }
	}

	@Test
	static public void test14()
	{
		// TODO Different way of forming tuples. Possibly through an operator ,

		test( "f = ( a ) -> a * a; f( 3 )", new BigDecimal( 9 ) );
		test( "( (a) -> a(3) ) ( (b) -> 5 * b )", new BigDecimal( 15 ) );
		test( "{ ( a, b ) -> a( 1, 2 ) * b( 3, 4 ) } ( ( a, b ) -> a * b, ( a, b ) -> a * b )", new BigDecimal( 24 ) );
		test( "a = 0; { () -> a = 1 } (); a", new BigDecimal( 0 ) ); // TODO This may not be right

		test( "f = () -> () -> 2; f()()", new BigDecimal( 2 ) );
		test( "f = () -> { 2 }; f()", new BigDecimal( 2 ) );
		test( "( () -> 2 )()", new BigDecimal( 2 ) );
		test( "{ () -> 2 }()", new BigDecimal( 2 ) );
	}
}
