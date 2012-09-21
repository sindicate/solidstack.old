package solidstack.script;

import solidstack.io.SourceException;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.Token.TYPE;

public class ScriptParser
{
	private ScriptTokenizer tokenizer;

	public ScriptParser( ScriptTokenizer t )
	{
		this.tokenizer = t;
	}

	public Identifier parse()
	{
		Identifier result = null;

		while( true )
		{
			Token token = this.tokenizer.get();
			if( token.getType() == TYPE.IDENTIFIER )
			{
				result = new Identifier( (String)token.getValue() );
			}
			else if( token.getType() == TYPE.EOF )
			{
				return result;
			}
			else
				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
		}
	}
}
