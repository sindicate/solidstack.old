package solidstack.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.Test;

import solidstack.util.Pars;

import bsh.EvalError;
import bsh.PreparsedScript;


public class BeanShellTest
{
	static public final String CONSTANT = "CONSTANT";

	@Test( groups = "new" )
	public void test() throws ScriptException, IOException, EvalError
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
				ScriptEngine engine = new ScriptEngineManager().getEngineByMimeType( "application/x-beanshell" );
				Writer out = new StringWriter();
				Bindings bindings = engine.createBindings();
				bindings.put( "test", "value" );
				bindings.put( "out", out );
				engine.eval( "out.write(test)", bindings );
				System.out.println( out.toString() );
			}
		}

		// TODO Use a StringResource created from the file
		BufferedReader in = new BufferedReader( new FileReader( "test/src/solidstack/template/test.bsh" ) );
		StringBuilder b = new StringBuilder();
		String line = in.readLine();
		while( line != null )
		{
			b.append( line ).append( '\n' );
			line = in.readLine();
		}
		PreparsedScript script = new PreparsedScript( b.toString() );
		Writer out = new StringWriter();
		script.invoke( new Pars( "out", new NoEncodingWriter( out ),
				"prefix", "prefix",
				"name", "name",
				"names", "names" ) );
		System.out.println( out.toString() );
	}
}
