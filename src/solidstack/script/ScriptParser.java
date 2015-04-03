/*--
 * Copyright 2012 Ren� M. de Bloois
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

import solidstack.io.PushbackReader;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.lang.Assert;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.TokenType;
import solidstack.script.StringTokenizer.Fragment;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.BooleanLiteral;
import solidstack.script.expressions.CharLiteral;
import solidstack.script.expressions.DecimalLiteral;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Expressions;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.If;
import solidstack.script.expressions.IntegerLiteral;
import solidstack.script.expressions.Module;
import solidstack.script.expressions.NullLiteral;
import solidstack.script.expressions.Parenthesis;
import solidstack.script.expressions.StringExpression;
import solidstack.script.expressions.StringLiteral;
import solidstack.script.expressions.SymbolExpression;
import solidstack.script.expressions.Throw;
import solidstack.script.expressions.Var;
import solidstack.script.expressions.While;
import solidstack.script.expressions.With;
import solidstack.script.operators.Operator;
import solidstack.script.operators.Spread;
import funny.Symbol;


/**
 * Parses a funny script.
 *
 * @author Ren� de Bloois
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

			case CHAR:
				return new CharLiteral( token.getLocation(), token.getValue().charAt( 0 ) );

			case DECIMAL:
				return new DecimalLiteral( token.getLocation(), new BigDecimal( token.getValue() ) );

			case INTEGER:
				return new IntegerLiteral( token.getLocation(), Integer.valueOf( token.getValue() ) );

			case STRING:
				return new StringLiteral( token.getLocation(), token.getValue() );

			case PSTRING:
				return parsePString( token, this.tokenizer.getIn() );

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
				return new NullLiteral( token.getLocation() );

			case WHILE:
				Token token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'while', not " + token2, token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				Expressions expressions = parseExpressions();
				swapStops( oldStop );
				Expression left = parseExpression();
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new While( token.getLocation(), expressions, left );

			case WITH:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'with', not " + token2, token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				expressions = parseExpressions();
				swapStops( oldStop );
				left = parseExpression();
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new With( token.getLocation(), expressions, left );

			case MODULE:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'module', not " + token2, token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				expressions = parseExpressions();
				swapStops( oldStop );
				left = parseExpression();
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new Module( token.getLocation(), expressions, left );

			case IF:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'if', not " + token2, token2.getLocation() );
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
				return new BooleanLiteral( token.getLocation(), true );

			case FALSE:
				return new BooleanLiteral( token.getLocation(), false );

			case VAR:
				token2 = this.tokenizer.next();
				if( token2.getType() == TokenType.IDENTIFIER )
					return new Var( token.getLocation(), new Identifier( token2.getLocation(), token2.getValue() ) );
				throw new SourceException( "identifier expected after 'var', not " + token2, token2.getLocation() );

			case THROW:
				Expression exception = parseExpression();
				if( exception == null )
					throw new SourceException( "expression expected after 'throw'", token.getLocation() );
				token2 = this.tokenizer.last();
				Assert.isTrue( token2.getType() == this.stop || token2.getType() == TokenType.EOF || token2.eq( ";" ), "Did not expect token " + token2 );
				this.tokenizer.push();
				return new Throw( token.getLocation(), exception );

			case IDENTIFIER:
			case RETURN: // TODO Make a statement instead of a function
			case VAL: // TODO Make a statement instead of a function
			case THIS:
//				if( token.getValue().equals( "fun" ) ) // TODO Remove this or use Scala's function syntax
//				{
//					token2 = this.tokenizer.next();
//					if( token2.getType() != TokenType.PAREN_OPEN && token2.getType() != TokenType.BRACE_OPEN )
//						throw new SourceException( "Expected one of (, {", token2.getLocation() );
//					if( token2.getType() == TokenType.PAREN_OPEN )
//						oldStop = swapStops( TokenType.PAREN_CLOSE );
//					else
//						oldStop = swapStops( TokenType.BRACE_CLOSE );
//					expressions = parseExpressions();
//					swapStops( oldStop );
//					if( expressions.size() < 2 )
//						throw new SourceException( "Expected 2 or more expressions", token2.getLocation() );
//					Expression pars = expressions.remove( 0 );
//					Expression block = token2.getType() == TokenType.BRACE_OPEN ? new Block( expressions.getLocation(), expressions ) : expressions;
//					return new Parenthesis( token2.getLocation(), new Function( "->", pars, block ) );
//				}

				return new Identifier( token.getLocation(), token.getValue() );

			case SYMBOL:
				return new SymbolExpression( token.getLocation(), Symbol.apply( token.getValue() ) );

			default:
				if( token.getType().isReserved() )
					throw new SourceException( "Unexpected reserved word " + token, token.getLocation() );
				throw new SourceException( "Unexpected token " + token, token.getLocation() );
		}
	}

	/**
	 * Parses a string as a processed string.
	 *
	 * @param s The processed string to parse.
	 * @param location The start location of the processed string.
	 * @return An expression.
	 */
	static public Expression parseString( String s, SourceLocation location )
	{
		SourceReader in = SourceReaders.forString( s, location );
		StringTokenizer t = new StringTokenizer( in, false );
		return parsePString( t, location );
	}

	/**
	 * Parses a processed string.
	 *
	 * @param token The identifier in front of the processed string.
	 * @param in The source reader to read the processed string from.
	 * @return An expression.
	 */
	static public Expression parsePString( Token token, PushbackReader in )
	{
		if( !token.eq( "s" ) )
			throw new SourceException( "Only 's' is currently allowed", token.getLocation() );
		StringTokenizer t = new StringTokenizer( in, true );
		return parsePString( t, token.getLocation() );
	}

	static private Expression parsePString( StringTokenizer t, SourceLocation location )
	{
		ScriptParser parser = new ScriptParser( t );
		parser.swapStops( TokenType.BRACE_CLOSE );

		StringExpression result = new StringExpression( location );

		Fragment fragment = t.getFragment();
		if( fragment.length() != 0 )
			result.append( new StringLiteral( fragment.getLocation(), fragment.getValue() ) );
		while( t.foundExpression() )
		{
			Expression expression = parser.parse();
			if( expression != null ) // TODO Unit test
				result.append( expression instanceof StringLiteral ? new Parenthesis( expression.getLocation(), expression ) : expression );
			fragment = t.getFragment();
			if( fragment.length() != 0 )
				result.append( new StringLiteral( fragment.getLocation(), fragment.getValue() ) );
		}

		if( result.size() == 0 )
			return new StringLiteral( fragment.getLocation(), "" );
		if( result.size() == 1 && result.get( 0 ) instanceof StringLiteral )
			return result.get( 0 );
		return result;
	}

	private TokenType swapStops( TokenType stop )
	{
		TokenType result = this.stop;
		this.stop = stop;
		return result;
	}
}
