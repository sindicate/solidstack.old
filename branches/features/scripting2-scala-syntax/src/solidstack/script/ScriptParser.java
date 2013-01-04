/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.script;

import java.math.BigDecimal;

import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.lang.Assert;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.TokenType;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.BooleanConstant;
import solidstack.script.expressions.DecimalConstant;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Expressions;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.If;
import solidstack.script.expressions.IntegerConstant;
import solidstack.script.expressions.NullConstant;
import solidstack.script.expressions.Parenthesis;
import solidstack.script.expressions.StringConstant;
import solidstack.script.expressions.StringExpression;
import solidstack.script.expressions.SymbolExpression;
import solidstack.script.expressions.While;
import solidstack.script.operators.Apply;
import solidstack.script.operators.Function;
import solidstack.script.operators.Operator;
import solidstack.script.operators.Spread;
import funny.Symbol;


/**
 * Parses a funny script.
 *
 * @author René de Bloois
 *
 */
public class ScriptParser
{
	private ScriptTokenizer tokenizer;
	private TokenType stop;
	private boolean expectElse;


	/**
	 * @param tokenizer The script tokenizer.
	 */
	public ScriptParser( ScriptTokenizer tokenizer )
	{
		this.tokenizer = tokenizer;
	}

	/**
	 * Parses everything.
	 *
	 * @return An expression.
	 */
	public Expression parse()
	{
		Expressions results = parseExpressions();
		if( results.size() == 0 )
			return null;
		if( results.size() == 1 )
			return results.get( 0 );
		return results;
	}

	/**
	 * Parses everything.
	 *
	 * @return An expression.
	 */
	private Expressions parseExpressions()
	{
		Expressions results = new Expressions();
		while( true )
		{
			Expression expression = parseExpression();
			Token last = this.tokenizer.last();
			if( last.getType() == TokenType.EOF || last.getType() == this.stop )
			{
				if( expression != null )
					results.append( expression );
				if( this.stop != null && last.getType() == TokenType.EOF )
					throw new SourceException( "Unexpected " + last + ", missing " + this.stop, last.getLocation() );
//				if( results.size() == 0 )
//					return null;
				return results;
			}
			results.append( expression );

		}
	}

	// Parses an expression (ends with ; or EOF)
	private Expression parseExpression()
	{
		Expression result = parseAtom();
		if( result == null )
			return null;

		while( true )
		{
			Token token = this.tokenizer.next();
			TokenType type = token.getType();
			if( type == this.stop )
				return result;

			switch( type )
			{
				case EOF:
					if( this.stop != null )
						throw new SourceException( "Unexpected " + token + ", missing " + this.stop, token.getLocation() );
					//$FALL-THROUGH$
				case SEMICOLON:
					return result;

				case OPERATOR:
				case COMMA:
				case DOT:
				case EQUALS:
				case HASH:
				case FUNCTION:
					Expression right = parseAtom();
					Assert.notNull( right );
					result = appendOperator( result, token.getValue(), right );
					break;

//				case BRACKET_OPEN:
				case PAREN_OPEN:
					TokenType oldStop = swapStops( inverse( type ) );
					Expression parameters = parse();
					swapStops( oldStop );
					result = appendOperator( result, token.getValue(), parameters );
					break;

				case ELSE:
					if( !this.expectElse )
						throw new SourceException( "Unexpected token '" + token + "'", token.getLocation() );
					this.tokenizer.push();
					return result;

				case IDENTIFIER:
					// TODO asInstanceOf and isInstanceOf? Or replace 'as' with toInt() etc.?
					if( token.getValue().equals( "as" ) || token.getValue().equals( "instanceof" ) )
					{
						right = parseAtom();
						Assert.notNull( right );
						result = appendOperator( result, token.getValue(), right );
						break;
					}
					//$FALL-THROUGH$

				default:
					throw new SourceException( "Unexpected token '" + token + "'", token.getLocation() );
			}
		}
	}

	private Expression appendOperator( Expression result, String operator, Expression operand )
	{
		if( result instanceof Operator )
			return ( (Operator)result ).append( operator, operand );
		return Operator.operator( operator, result, operand );
	}

	private TokenType inverse( TokenType type )
	{
		switch( type )
		{
			case BRACE_OPEN:
				return TokenType.BRACE_CLOSE;
			case BRACKET_OPEN:
				return TokenType.BRACKET_CLOSE;
			case PAREN_OPEN:
				return TokenType.PAREN_CLOSE;
			default:
				throw new AssertionError();
		}
	}

	/**
	 * @return The smallest expression possible.
	 */
	private Expression parseAtom()
	{
		Token token = this.tokenizer.next();
		TokenType type = token.getType();
		if( type == this.stop )
			return null;

		switch( type )
		{
			case EOF:
				if( this.stop != null )
					throw new SourceException( "Unexpected " + token + ", missing " + this.stop, token.getLocation() );
				//$FALL-THROUGH$
			case SEMICOLON:
				return null;

			case DECIMAL:
				return new DecimalConstant( token.getLocation(), new BigDecimal( token.getValue() ) );

			case INTEGER:
				return new IntegerConstant( token.getLocation(), Integer.valueOf( token.getValue() ) );

			case STRING:
				return parseString( token );

			case PAREN_OPEN:
				TokenType oldStop = swapStops( TokenType.PAREN_CLOSE );
				Expression result = parse();
				swapStops( oldStop );
				return new Parenthesis( token.getLocation(), result );

			case BRACE_OPEN:
				oldStop = swapStops( TokenType.BRACE_CLOSE );
				result = parse();
				swapStops( oldStop );
				return new Block( token.getLocation(), result );

//			case BRACKET_OPEN:
//				Token token2 = this.tokenizer.get();
//				if( token2.eq( ":" ) )
//				{
//					token2 = this.tokenizer.get();
//					Assert.isTrue( token2.getType() == TOKENTYPE.BRACKET_CLOSE, "Not expecting token " + token2 );
//					return new EmptyMap( token.getLocation() );
//				}
//				this.tokenizer.push();
//				oldStop = swapStops( TOKENTYPE.BRACKET_CLOSE );
//				result = parse();
//				swapStops( oldStop );
//				return new solidstack.script.expressions.List( token.getLocation(), result );

			case OPERATOR:
				// No need to consider precedences here. Only one atom is parsed.
				if( token.getValue().equals( "-" ) )
					return Operator.preOp( token.getLocation(), "-@", parseAtom() ); // TODO Pre-apply
				if( token.getValue().equals( "!" ) )
					return Operator.preOp( token.getLocation(), "!@", parseAtom() ); // TODO Pre-apply
				if( token.getValue().equals( "+" ) )
					return parseAtom(); // TODO Is this correct, just ignore the operator?
				if( token.getValue().equals( "*" ) )
					return new Spread( token.getLocation(), token.getValue(), parseAtom() );
				throw new SourceException( "Unexpected token " + token, token.getLocation() );

			case NULL:
				return new NullConstant( token.getLocation() );

			case WHILE:
				Token token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis", token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				Expressions expressions = parseExpressions();
				swapStops( oldStop );
				Expression left = parseExpression();
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new While( token.getLocation(), expressions, left );

			case IF:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis", token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				expressions = parseExpressions();
				swapStops( oldStop );
				this.expectElse = true;
				left = parseExpression();
				this.expectElse = false;
				Expression right = null;
				token2 = this.tokenizer.next();
				if( token2.getType() == TokenType.ELSE )
					right = parseExpression();
				else
					this.tokenizer.push();
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new If( token.getLocation(), expressions, left, right );

			case NEW:
				return Operator.preOp( token.getLocation(), "new", parseAtom() );

			case TRUE:
				return new BooleanConstant( token.getLocation(), true );

			case FALSE:
				return new BooleanConstant( token.getLocation(), false );

			case VAR:
				return new Apply( "(", new Identifier( token.getLocation(), "def" ), parseAtom() );

			case IDENTIFIER:
			case THROW: // TODO Make a statement instead of a function
			case RETURN: // TODO Make a statement instead of a function
			case VAL: // TODO Make a statement instead of a function
			case THIS:
				if( token.getValue().equals( "fun" ) ) // TODO Remove this or use Scala's function syntax
				{
					token2 = this.tokenizer.next();
					if( token2.getType() != TokenType.PAREN_OPEN && token2.getType() != TokenType.BRACE_OPEN )
						throw new SourceException( "Expected one of (, {", token2.getLocation() );
					if( token2.getType() == TokenType.PAREN_OPEN )
						oldStop = swapStops( TokenType.PAREN_CLOSE );
					else
						oldStop = swapStops( TokenType.BRACE_CLOSE );
					expressions = parseExpressions();
					swapStops( oldStop );
					if( expressions.size() < 2 )
						throw new SourceException( "Expected 2 or more expressions", token2.getLocation() );
					Expression pars = expressions.remove( 0 );
					Expression block = token2.getType() == TokenType.BRACE_OPEN ? new Block( expressions.getLocation(), expressions ) : expressions;
					return new Parenthesis( token2.getLocation(), new Function( "->", pars, block ) );
				}

				return new Identifier( token.getLocation(), token.getValue() );

			case SYMBOL:
				return new SymbolExpression( token.getLocation(), Symbol.apply( token.getValue() ) );

			default:
				throw new SourceException( "Unexpected token " + token, token.getLocation() );
		}
	}

	/**
	 * Parses a super string.
	 *
	 * @param s The super string to parse.
	 * @param location The location of the super string.
	 * @return An expression.
	 */
	static public Expression parseString( String s, SourceLocation location )
	{
		SourceReader in = SourceReaders.forString( s, location );
		StringTokenizer t = new StringTokenizer( in );
		ScriptParser parser = new ScriptParser( t );
		parser.swapStops( TokenType.BRACE_CLOSE );

		StringExpression result = new StringExpression( location );

		String fragment = t.getFragment();
		if( fragment.length() != 0 )
			result.append( new StringConstant( location, fragment ) );
		while( t.foundExpression() )
		{
			Expression expression = parser.parse();
			if( expression != null ) // TODO Unit test
				result.append( expression );
			fragment = t.getFragment();
			if( fragment.length() != 0 )
				result.append( new StringConstant( location, fragment ) );
		}

		if( result.size() == 0 )
			return new StringConstant( location, "" );
		if( result.size() == 1 && result.get( 0 ) instanceof StringConstant )
			return result.get( 0 );
		return result;
	}

	/**
	 * Parses a super string.
	 *
	 * @param string The super string to parse.
	 * @return An expression.
	 */
	static public Expression parseString( Token string )
	{
		return parseString( string.getValue(), string.getLocation() );
	}

	private TokenType swapStops( TokenType stop )
	{
		TokenType result = this.stop;
		this.stop = stop;
		return result;
	}
}
