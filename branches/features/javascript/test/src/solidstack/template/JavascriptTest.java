package solidstack.template;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.Resource;
import solidbase.io.ResourceFactory;


public class JavascriptTest
{
	static public final String CONSTANT = "CONSTANT";

	@Test//( groups = "new" )
	public void test() throws ScriptException, IOException
	{
		{
			ScriptEngineManager manager = new ScriptEngineManager();

			for( ScriptEngineFactory factory : manager.getEngineFactories() )
			{
				System.out.println( factory.getEngineName() + ": " + factory.getLanguageName() );
				for( String mimes : factory.getMimeTypes() )
					System.out.println( "\t: " + mimes );
				System.out.println( factory.getOutputStatement( "test" ) );
			}

			{
				ScriptEngine engine = new ScriptEngineManager().getEngineByMimeType( "application/javascript" );
				Writer out = new StringWriter();
				Bindings bindings = engine.createBindings();
				bindings.put( "test", "value" );
				bindings.put( "out", out );
				engine.eval( "out.write(test)", bindings );
				System.out.println( out.toString() );
			}
		}

		Context cx = Context.enter();
		try
		{
			cx.setOptimizationLevel( -1 );

			{
				Script script = cx.compileString( "f(\"x\");\"test\"", "<cmd>", 1, null );
				ScriptableObject scope = cx.initStandardObjects();
				scope.defineFunctionProperties( new String[] { "f" }, JavascriptTest.class, ScriptableObject.DONTENUM );
//				Object result = cx.evaluateString(scope, "f(\"x\");\"test\"", "<cmd>", 1, null);
				Object result = cx.executeScriptWithContinuations( script, scope );
				System.out.println( cx.toString( result ) );
			}

			{
				TopLevel scope = new ImporterTopLevel(cx);
				Script script = cx.compileReader( new FileReader( "test/src/solidstack/template/test.js" ), "<cmd>", 1, null );
				Writer out = new StringWriter();
				scope.put( "out", scope, new NoEncodingWriter( out ) );
				scope.put( "prefix", scope, "prefix" );
				scope.put( "name", scope, "name" );
				scope.put( "names", scope, "names" );
				cx.executeScriptWithContinuations( script, scope );
				System.out.println( out.toString() );
			}
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

	@Test//(groups="new")
	public void testTransform() throws Exception
	{
		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/template/testjs.gtext" );
		Template template = new TemplateCompiler().translate( "p", "c", new BOMDetectingLineReader( resource ) );
		System.out.println( template.getSource().replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
		Assert.assertEquals( template.getSource(), "importClass(Packages.java.sql.Timestamp);importPackage(Packages.java.util);\n" +
				" // Test if the import at the bottom works, and this comment too of course\n" +
				"new Timestamp( new java.util.Date().time ) \n" +
				";out.write(\"SELECT *\\n\\\n" +
				"\");\n" +
				"out.write(\"FROM SYS.SYSTABLES\");out.write( null );out.write(\"\\n\\\n" +
				"\");\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"WHERE 1 = 1\\n\\\n" +
				"\"); if( prefix ) { \n" +
				";out.write(\"AND TABLENAME LIKE '\");out.write( prefix );out.write(\"%'\\n\\\n" +
				"\"); } \n" +
				"; if( name ) { \n" +
				";out.write(\"AND TABLENAME = \");out.writeEncoded(name);out.write(\"\\n\\\n" +
				"\"); } \n" +
				"; if( names ) { \n" +
				";out.write(\"AND TABLENAME IN (\");out.writeEncoded(names);out.write(\")\\n\\\n" +
				"\"); } \n" +
				";\n" );

		TemplateManager queries = new TemplateManager();
		queries.setPackage( "solidstack.template" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		params.put( "name", null );
		params.put( "names", null );
		template = queries.getTemplate( "testjs.gtext" );
		String result = template.apply( params );

		Writer out = new OutputStreamWriter( new FileOutputStream( "test2.out" ), "UTF-8" );
		out.write( result );
		out.close();

		Assert.assertEquals( result, "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME LIKE 'SYST%'\n" );
	}
}
