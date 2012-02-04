package solidstack.template;

import java.io.StringWriter;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.testng.annotations.Test;

public class JavascriptTest
{
	@Test(groups="new")
	public void test() throws ScriptException
	{
		ScriptEngineManager manager = new ScriptEngineManager();

		for( ScriptEngineFactory factory : manager.getEngineFactories() )
		{
			System.out.println( factory.getEngineName() + ": " + factory.getLanguageName() );
			for( String mimes : factory.getMimeTypes() )
				System.out.println( "\t: " + mimes );
			System.out.println( factory.getOutputStatement( "test" ) );
		}

		ScriptEngine engine = new ScriptEngineManager().getEngineByMimeType( "application/javascript" );
		Writer out = new StringWriter();
		Bindings bindings = engine.createBindings();
		bindings.put( "test", "value" );
		bindings.put( "out", out );
		engine.eval( "out.write(test)", bindings );
		System.out.println( out.toString() );

		Context cx = Context.enter();
		cx.setOptimizationLevel( -1 );
		Script script = cx.compileString( "f(\"x\");\"test\"", "<cmd>", 1, null );
		try
		{
			ScriptableObject scope = cx.initStandardObjects();
			scope.defineFunctionProperties( new String[] { "f" }, JavascriptTest.class,  ScriptableObject.DONTENUM );
//			Object result = cx.evaluateString(scope, "f(\"x\");\"test\"", "<cmd>", 1, null);
			Object result = cx.executeScriptWithContinuations(script, scope);
			System.out.println(cx.toString(result));
		}
		finally
		{
			Context.exit();
		}
	}

	static public void f( String test )
	{
		System.out.println( test );
	}
}
