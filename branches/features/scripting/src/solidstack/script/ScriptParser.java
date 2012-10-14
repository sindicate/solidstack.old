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
				Expressions parameters = new Expressions();
				do
					parameters.append( parse( ",", ")" ) );
				while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
				if( result instanceof Operation )
					result = ( (Operation)result ).append( token.getValue(), parameters );
				else
					result = Operation.operation( token.getValue(), result, parameters );
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
						result = Operation.operation( result, first, second );
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
						result = Operation.operation( token.getValue(), result, right );
				}
			}
			else if( token.getType() == TYPE.UNAOP )
			{
				Assert.isTrue( result != null );
				if( result instanceof Operation )
					result = ( (Operation)result ).append( "@" + token.getValue(), null );
				else
					result = Operation.operation( "@" + token.getValue(), result, null );
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
			if( token.getValue().equals( "while" ) )
			{
				Expression result = parseOne( true );
				if( !( result instanceof Parenthesis ) )
					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
				Expression left = parseOne( true );
				return new While( ( (Parenthesis)result ).getExpression(), left );
			}
			if( token.getValue().equals( "function" ) )
			{
				token = this.tokenizer.get();
				if( token.getType() != TYPE.PAREN_OPEN )
					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
				List<String> parameters = new ArrayList<String>();
				do
				{
					Expression parameter = parse( ",", ")" );
					if( !( parameter instanceof Identifier ) )
						throw new SourceException( "Expected am identifier", this.tokenizer.getLocation() );
					parameters.add( ( (Identifier)parameter ).getName() );
				}
				while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
				Expression block = parseOne( true );
				return new FunctionSpec( parameters, block );
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
				return Operation.operation( "-@", result, null );
			}
			else if( token.getValue().equals( "+" ) )
			{
				return parseOne( false );
			}
		if( token.getType() == TYPE.UNAOP )
		{
			if( token.getValue().equals( "!" ) )
			{
				Expression result = parseOne( false );
				if( result instanceof BooleanConstant )
					return ( (BooleanConstant)result ).not();
				return Operation.operation( "!@", null, result );
			}
			Expression result = parseOne( false );
			return Operation.operation( token.getValue() + "@", null, result );
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
