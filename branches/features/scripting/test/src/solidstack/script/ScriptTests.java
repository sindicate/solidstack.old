package solidstack.script;

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
}
