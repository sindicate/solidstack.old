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
import solidstack.script.ScriptTokenizer.Token.TYPE;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.BooleanLiteral;
import solidstack.script.expressions.CharLiteral;
import solidstack.script.expressions.DecimalLiteral;
import solidstack.script.expressions.Defined;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Expressions;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.If;
import solidstack.script.expressions.IntegerLiteral;
import solidstack.script.expressions.Module;
import solidstack.script.expressions.NullLiteral;
import solidstack.script.expressions.Parenthesis;
import solidstack.script.expressions.StringConstant;
import solidstack.script.expressions.StringExpression;
import solidstack.script.expressions.SymbolExpression;
import solidstack.script.expressions.Throw;
import solidstack.script.expressions.Var;
import solidstack.script.expressions.While;
import solidstack.script.expressions.With;
import solidstack.script.operators.Operator;
import solidstack.script.operators.Spread;


/**
 * Parses a funny script.
 *
 * @author René de Bloois
 *
 */
public class ScriptParser
{
	private ScriptTokenizer tokenizer;
	private TYPE stop;


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
			Token last = this.tokenizer.lastToken();
			if( last.getType() == TYPE.EOF || last.getType() == this.stop )
			{
				if( expression != null )
					results.append( expression );
				if( this.stop != null && last.getType() == TYPE.EOF )
					throw new SourceException( "Unexpected " + last + ", missing " + this.stop, last.getLocation() );
//				if( results.size() == 0 )
//					return null;
				return results;
			}
			results.append( expression );

		}
	}

	// Parses on expression (ends with ; or EOF)
	private Expression parseExpression()
	{
		Expression result = parseAtom();
		if( result == null ) // TODO What if no atom because of ,
			return null;

		while( true )
		{
			Token token = this.tokenizer.get();
			TYPE type = token.getType();
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

				case BINOP:
					Expression right = parseAtom();
					Assert.notNull( right );
					result = appendOperator( result, token.getValue(), right );
					break;

				case BRACKET_OPEN:
				case PAREN_OPEN:
					TYPE oldStop = swapStops( inverse( type ) );
					Expression parameters = parse();
					swapStops( oldStop );
					result = appendOperator( result, token.getValue(), parameters );
					break;

				case UNAOP:
					result = appendOperator( result, "@" + token.getValue(), null );
					break;

				case IDENTIFIER:
					if( token.getValue().equals( "as" ) || token.getValue().equals( "instanceof" ) )
					{
						right = parseAtom();
						Assert.notNull( right );
						result = appendOperator( result, token.getValue(), right );
						break;
					} //$FALL-THROUGH$

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

	private TYPE inverse( TYPE type )
	{
		switch( type )
		{
			case BRACE_OPEN:
				return TYPE.BRACE_CLOSE;
			case BRACKET_OPEN:
				return TYPE.BRACKET_CLOSE;
			case PAREN_OPEN:
				return TYPE.PAREN_CLOSE;
			default:
				throw new AssertionError();
		}
	}

	/**
	 * @return The smallest expression possible.
	 */
	private Expression parseAtom()
	{
		Token token = this.tokenizer.get();
		TYPE type = token.getType();
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
				return new DecimalConstant( token.getLocation(), new BigDecimal( token.getValue() ) );

			case INT:
				return new IntegerConstant( token.getLocation(), Integer.valueOf( token.getValue() ) );

			case STRING:
				return parseString( token );

			case PAREN_OPEN:
				TYPE oldStop = swapStops( TYPE.PAREN_CLOSE );
				Expression result = parse();
				swapStops( oldStop );
				return new Parenthesis( token.getLocation(), result );

			case BRACE_OPEN:
				oldStop = swapStops( TYPE.BRACE_CLOSE );
				result = parse();
				swapStops( oldStop );
				return new Block( token.getLocation(), result );

			case BINOP:
				// No need to consider precedences here. Only one atom is parsed.
				if( token.getValue().equals( "-" ) )
				{
					result = parseAtom(); // TODO Pre-apply
					return Operator.preOp( token.getLocation(), "-@", result );
				}
				if( token.getValue().equals( "+" ) )
					return parseAtom(); // TODO Is this correct, just ignore the operator?
				if( token.getValue().equals( "*" ) )
					return new Spread( token.getLocation(), token.getValue(), parseAtom() );
				if( token.getValue().equals( ":" ) )
					return new SymbolExpression( token.getLocation(), parseAtom() );
				throw new SourceException( "Unexpected token " + token, token.getLocation() );

			case IDENTIFIER:
				String name = token.getValue();
				if( name.equals( "false" ) )
					return new BooleanConstant( token.getLocation(), false );

				if( name.equals( "true" ) )
					return new BooleanConstant( token.getLocation(), true );

				if( name.equals( "null" ) )
					return new NullConstant( token.getLocation() );

				if( token.getValue().equals( "if" ) )
				{
					token2 = this.tokenizer.get();
					if( token2.getType() != TYPE.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'while', not " + token2, token2.getLocation() );
					oldStop = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parseExpressions();
					swapStops( oldStop );
					if( expressions.size() != 2 && expressions.size() != 3 )
						throw new SourceException( "Expected 2 or 3 expressions", token2.getLocation() );
					return new If( token.getLocation(), expressions.get( 0 ), expressions.get( 1 ), expressions.size() == 3 ? expressions.get( 2 ) : null );
				}

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

			case DEFINED:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'defined', not " + token2, token2.getLocation() );
				oldStop = swapStops( TokenType.PAREN_CLOSE );
				expressions = parseExpressions();
				swapStops( oldStop );
				return new Defined( token.getLocation(), expressions );

			case IF:
				token2 = this.tokenizer.next();
				if( token2.getType() != TokenType.PAREN_OPEN )
					throw new SourceException( "Expected an opening parenthesis after 'if', not " + token2, token2.getLocation() );
					oldStop = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parseExpressions();
					swapStops( oldStop );
					if( expressions.size() < 1 )
						throw new SourceException( "Expected at least 1 expression", token2.getLocation() );
					return new While( token.getLocation(), expressions.remove( 0 ), expressions );
				}

				// TODO Remove this
				if( token.getValue().equals( "fun" ) )
				{
					token2 = this.tokenizer.get();
					if( token2.getType() != TYPE.PAREN_OPEN && token2.getType() != TYPE.BRACE_OPEN )
						throw new SourceException( "Expected one of (, {", token2.getLocation() );
					if( token2.getType() == TYPE.PAREN_OPEN )
						oldStop = swapStops( TYPE.PAREN_CLOSE );
					else
						oldStop = swapStops( TYPE.BRACE_CLOSE );
					Expressions expressions = parseExpressions();
					swapStops( oldStop );
					if( expressions.size() < 2 ) // TODO And 1?
						throw new SourceException( "Expected 2 or more expressions", token2.getLocation() );
					Expression pars = expressions.remove( 0 );
					Expression block = token2.getType() == TYPE.BRACE_OPEN ? new Block( expressions.getLocation(), expressions ) : expressions;
					return new Parenthesis( token2.getLocation(), new Function( "->", pars, block ) );
				}

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
				return new Identifier( token.getLocation(), token.getValue() );

			case UNAOP:
				result = parseAtom();
				return Operator.preOp( token.getLocation(), token.getValue() + "@", result ); // TODO Pre-apply

			default:
				if( token.getType().isReserved() )
					throw new SourceException( "Unexpected reserved word " + token, token.getLocation() );
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
		parser.swapStops( TYPE.BRACE_CLOSE );

		StringExpression result = new StringExpression( location );

		Fragment fragment = t.getFragment(); // TODO Fragment object not needed anymore
		if( fragment.length() != 0 )
			result.appendFragment( fragment.getValue() );
		while( t.foundExpression() )
		{
			Expression expression = parser.parse();
			if( expression != null ) // TODO Unit test
				result.append( expression );
			fragment = t.getFragment();
			if( fragment.length() != 0 )
				result.appendFragment( fragment.getValue() );
		}

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

	private TYPE swapStops( TYPE stop )
	{
		TYPE result = this.stop;
		this.stop = stop;
		return result;
	}
}
