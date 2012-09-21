package solidstack.script;

import java.io.StringReader;
import java.util.Map;

import solidstack.io.ReaderSourceReader;

public class Script
{
	private Identifier identifier;

	public Script( Identifier identifier )
	{
		this.identifier = identifier;
	}

	// TODO Add location
	static public Script compile( String script )
	{
		ScriptTokenizer t = new ScriptTokenizer( new ReaderSourceReader( new StringReader( script ) ) );
		ScriptParser p = new ScriptParser( t );
		Identifier result = p.parse();
		return new Script( result );
	}

	public Object execute( Map<String, Object> context )
	{
		return context.get( this.identifier.getName() );
	}
}
