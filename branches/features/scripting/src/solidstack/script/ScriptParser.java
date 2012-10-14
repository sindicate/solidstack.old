package solidstack.script;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

	public Expression parse( String stop, String stop2 )
	{
		Expressions results = null;
		Expression result = parseOne( true );

		while( true )
		{
			Token token = this.tokenizer.get();
			if( stop == null )
			{
				if( token.getType() == TYPE.EOF )
				{
					if( results == null )
						return result;
					if( result != null )
						results.append( result );
					return results;
				}
			}
			else if( token.eq( stop ) || token.eq( stop2 ) )
			{
				if( results == null )
					return result;
				if( result != null )
					results.append( result );
				return results;
			}
			if( token.getType() == TYPE.SEMICOLON )
			{
				if( results == null )
					results = new Expressions();
				results.append( result );
				result = parseOne( true );
				continue;
			}
			if( token.getType() == TYPE.PAREN_OPEN )
			{
				Assert.isTrue( result != null );
				List<Expression> parameters = new ArrayList<Expression>();
				do
					parameters.add( parse( ",", ")" ) );
				while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
				result = result.append( parameters );
			}
			else if( token.getType() == TYPE.BINOP )
			{
				Assert.isTrue( result != null );
				if( token.getValue().equals( "?" ) )
				{
					Expression first = parse( ":", null );
					Expression second = parseOne( false );
					if( second == null )
						throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
					if( result instanceof Operation )
						result = ( (Operation)result ).append( first, second );
					else
						result = new Operation( result, first, second );
				}
				else
				{
					Assert.isFalse( token.getValue().equals( ":" ) );
					Expression right = parseOne( false );
					if( right == null )
						throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
					if( result instanceof Operation )
						result = ( (Operation)result ).append( token.getValue(), right );
					else
						result = new Operation( token.getValue(), result, right );
				}
			}
			else if( token.getType() == TYPE.UNAOP )
			{
				Assert.isTrue( result != null );
				if( result instanceof Operation )
					result = ( (Operation)result ).append( "@" + token.getValue(), null );
				else
					result = new Operation( "@" + token.getValue(), result, null );
			}
			else
				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
		}
	}

	public Expression parseOne( boolean start )
	{
		Token token = this.tokenizer.get();
		if( start )
			while( token.getType() == TYPE.SEMICOLON )
				token = this.tokenizer.get();
		if( token.getType() == TYPE.IDENTIFIER )
		{
			String name = token.getValue();
			if( name.equals( "false" ) )
				return new BooleanConstant( false );
			if( name.equals( "true" ) )
				return new BooleanConstant( true );
			if( name.equals( "null" ) )
				return new NullConstant();
			if( token.getValue().equals( "if" ) )
			{
				Expression result = parseOne( true );
				if( !( result instanceof Parenthesis ) )
					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
				Expression left = parseOne( true );
				token = this.tokenizer.get();
				if( token.getType() != TYPE.IDENTIFIER || !token.getValue().equals( "else" ) )
				{
					this.tokenizer.push();
					return new If( ( (Parenthesis)result ).getExpression(), left, null );
				}
				Expression right = parseOne( true );
				return new If( ( (Parenthesis)result ).getExpression(), left, right );
			}
			return new Identifier( token.getValue() );
		}
		if( token.getType() == TYPE.NUMBER )
			return new NumberConstant( new BigDecimal( token.getValue() ) );
		if( token.getType() == TYPE.STRING )
			return new StringConstant( token.getValue() );
		if( token == Token.PAREN_OPEN )
			return new Parenthesis( parse( ")", null ) );
		if( token.getType() == TYPE.BINOP )
			if( token.getValue().equals( "-" ) )
			{
				Expression result = parseOne( false );
				if( result instanceof NumberConstant )
					return ( (NumberConstant)result ).negate();
				return new Operation( "-@", result, null );
			}
			else if( token.getValue().equals( "+" ) )
			{
				return parseOne( false );
			}
		if( token.getType() == TYPE.UNAOP )
			if( token.getValue().equals( "!" ) )
			{
				Expression result = parseOne( false );
				if( result instanceof BooleanConstant )
					return ( (BooleanConstant)result ).not();
				return new Operation( "!@", null, result );
			}
			else
			{
				Expression result = parseOne( false );
				return new Operation( token.getValue() + "@", null, result );
			}
		if( token.getType() == TYPE.EOF )
			return null;
		if( token.getType() == TYPE.BRACE_OPEN )
			return parseBlock();
		throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
	}

	public Expression parseBlock()
	{
		return parse( "}", null );
	}
}
