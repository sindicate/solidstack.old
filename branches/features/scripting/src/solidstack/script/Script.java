package solidstack.script;

import java.io.StringReader;
import java.util.HashMap;
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
		if( context == null )
			context = new HashMap<String, Object>();
		if( this.expression == null )
			return null;

		// TODO Add unwrap() method somewhere
		Object result = this.expression.evaluate( context );
		if( result instanceof Value )
			return ( (Value)result ).get();
		return result;
	}
}