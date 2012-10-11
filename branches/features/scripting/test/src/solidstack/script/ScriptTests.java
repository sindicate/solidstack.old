package solidstack.script;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScriptTests
{
	@Test
	static public void test1()
	{
		Map<String, Object> context = new HashMap<String, Object>();
		context.put( "var1", "Value" );
		Script script = Script.compile( "var1" );
		Object result = script.execute( context );
		Assert.assertEquals( result, "Value" );
	}

	@Test
	static public void test2()
	{
		Map<String, Object> context = new HashMap<String, Object>();
		context.put( "var1", 1 );
		Script script = Script.compile( "var1 + 1" );
		Object result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 2 ) );
	}

	@Test
	static public void test3()
	{
		Map<String, Object> context = new HashMap<String, Object>();

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

	static private void test( String expression, Map<String, Object> context, Object expected )
	{
		Script script = Script.compile( expression );
		Object result = script.execute( context );
		Assert.assertEquals( result, expected );
	}

	@Test
	static public void test4()
	{
		Map<String, Object> context = new HashMap<String, Object>();

		int val = 1;
		Assert.assertEquals( val > 0 ? 2 : 3 + 1, 2 );
		Assert.assertEquals( 1 + 1 > 0 ? 2 : 3 + 1, 2 );
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
		Map<String, Object> context = new HashMap<String, Object>();

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
		test( "a = 0; b = 1", new BigDecimal( 1 ) );
//		test( "a = 0;", new BigDecimal( 0 ) );
	}
}
