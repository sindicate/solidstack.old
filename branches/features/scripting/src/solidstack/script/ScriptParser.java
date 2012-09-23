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

	public Expression parse( String stop )
	{
		Expression result = parseOne();

		while( true )
		{
			Token token = this.tokenizer.get();
			if( token.eq( stop ) )
				return result;
			if( stop == null && token.getType() == TYPE.EOF )
				return result;
			if( token.getType() == TYPE.PAREN_OPEN )
			{
				Assert.isTrue( result instanceof Identifier );
				Expression parameter = parse( ")" );
				result = new Function( ( (Identifier)result ).getName(), parameter );
			}
			else if( token.getType() == TYPE.OPERATION )
			{
				Assert.isTrue( result != null );
				if( token.getValue().equals( "?" ) )
				{
					Expression first = parse( ":" );
					Expression second = parseOne();
					if( result instanceof Operation )
						result = ( (Operation)result ).append( first, second );
					else
						result = new Operation( result, first, second );
				}
				else
				{
					Assert.isFalse( token.getValue().equals( ":" ) );
					Expression right = parseOne();
					if( result instanceof Operation )
						result = ( (Operation)result ).append( token.getValue(), right );
					else
						result = new Operation( token.getValue(), result, right );
				}
			}
			else
				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
		}
	}

	public Expression parseOne()
	{
		Token token = this.tokenizer.get();
		if( token.getType() == TYPE.IDENTIFIER )
			return new Identifier( token.getValue() );
		if( token.getType() == TYPE.NUMBER )
			return new Number( new BigDecimal( token.getValue() ) );
		if( token.getType() == TYPE.STRING )
			return new StringConstant( token.getValue() );
		if( token == Token.PAREN_OPEN )
			return new Parenthesis( parse( ")" ) );
		if( token.getType() == TYPE.OPERATION )
			if( token.getValue().equals( "-" ) )
			{
				Expression result = parseOne();
				Assert.isInstanceOf( result, Number.class );
				return ( (Number)result ).negate();
			}
			else if( token.getValue().equals( "+" ) )
			{
				Expression result = parseOne();
				Assert.isInstanceOf( result, Number.class );
				return result;
			}
		throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
	}
}
