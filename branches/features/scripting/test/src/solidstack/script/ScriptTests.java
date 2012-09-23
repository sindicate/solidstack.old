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
}
