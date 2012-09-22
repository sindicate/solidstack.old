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

		Script script = Script.compile( "1 + 1 * 2" );
		Object result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 3 ) );

		script = Script.compile( "2 * 1 + 1" );
		result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 3 ) );

		script = Script.compile( "1 + 1 + 1" );
		result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 3 ) );

		script = Script.compile( "1 + 2 * 2 + 1" );
		result = script.execute( context );
		Assert.assertEquals( result, new BigDecimal( 6 ) );
	}
}
