package solidstack.script;

import java.math.BigDecimal;

import solidstack.io.SourceException;
import solidstack.lang.Assert;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.Token.TYPE;

public class ScriptParser
{
	private ScriptTokenizer tokenizer;

	public ScriptParser( ScriptTokenizer t )
	{
		this.tokenizer = t;
	}

	public Expression parse( boolean ternairyIf )
	{
		Expression result = null;

		while( true )
		{
			Token token = this.tokenizer.get();
			if( token.getType() == TYPE.IDENTIFIER )
			{
				Assert.isTrue( result == null );
				result = new Identifier( (String)token.getValue() );
			}
			else if( token.getType() == TYPE.NUMBER )
			{
				Assert.isTrue( result == null );
				result = new Number( (BigDecimal)token.getValue() );
			}
			else if( token.getType() == TYPE.OPERATOR )
			{
				Assert.isTrue( result != null );
				if( token.getValue().equals( "?" ) )
				{
					Expression first = parse( true );
//					token = this.tokenizer.get();
//					Assert.isTrue( token.getType() == TYPE.OPERATOR );
//					Assert.isTrue( token.getValue().equals( ":" ) );
					Expression second = parseOne();
					if( result instanceof Operation )
						result = ( (Operation)result ).append( first, second );
					else
						result = new Operation( result, first, second );
				}
				else
				{
					if( ternairyIf && token.getValue().equals( ":" ) )
						return result;
					Assert.isFalse( token.getValue().equals( ":" ) );
					Expression right = parseOne();
					if( result instanceof Operation )
						result = ( (Operation)result ).append( (String)token.getValue(), right );
					else
						result = new Operation( (String)token.getValue(), result, right );
				}
			}
			else if( !ternairyIf && token.getType() == TYPE.EOF )
			{
				return result;
			}
			else
				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
		}
	}

	public Expression parseOne()
	{
		Token token = this.tokenizer.get();
		if( token.getType() == TYPE.IDENTIFIER )
		{
			return new Identifier( (String)token.getValue() );
		}
		else if( token.getType() == TYPE.NUMBER )
		{
			return new Number( (BigDecimal)token.getValue() );
		}
		throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
	}
}
