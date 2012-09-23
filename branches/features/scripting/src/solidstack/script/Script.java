package solidstack.script;

import java.io.StringReader;
import java.util.Map;

import solidstack.io.ReaderSourceReader;

public class Script
{
	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	// TODO Add location
	static public Script compile( String script )
	{
		ScriptTokenizer t = new ScriptTokenizer( new ReaderSourceReader( new StringReader( script ) ) );
		ScriptParser p = new ScriptParser( t );
		Expression result = p.parse( null, null );
		return new Script( result );
	}

	public Object execute( Map<String, Object> context )
	{
		return this.expression.evaluate( context );
	}
}
