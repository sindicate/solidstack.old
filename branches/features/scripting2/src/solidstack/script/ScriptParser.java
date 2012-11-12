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
import java.util.ArrayList;
import java.util.List;

import solidstack.io.SourceException;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.lang.Assert;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.Token.TYPE;
import solidstack.script.expressions.Block;
import solidstack.script.expressions.BooleanConstant;
import solidstack.script.expressions.EmptyMap;
import solidstack.script.expressions.Expression;
import solidstack.script.expressions.Expressions;
import solidstack.script.expressions.Function;
import solidstack.script.expressions.Identifier;
import solidstack.script.expressions.If;
import solidstack.script.expressions.NullConstant;
import solidstack.script.expressions.NumberConstant;
import solidstack.script.expressions.Operation;
import solidstack.script.expressions.StringConstant;
import solidstack.script.expressions.StringExpression;
import solidstack.script.expressions.Tuple;
import solidstack.script.expressions.While;


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
	public Expressions parse()
	{
		Expressions results = new Expressions();
		Tuple lastTuple = null;
		while( true )
		{
			if( lastTuple != null )
				if( lastTuple == Tuple.EMPTY_TUPLE )
					results.append( null );
				else if( lastTuple.size() == 1 )
					results.append( lastTuple.get( 0 ) );
				else
					results.append( lastTuple );
			lastTuple = parseTuple();
			Assert.isTrue( lastTuple != null  );
			Token last = this.tokenizer.lastToken();
			if( last.getType() == TYPE.EOF || last.getType() == this.stop )
			{
				if( this.stop != null && last.getType() == TYPE.EOF )
					throw new SourceException( "Unexpected " + last + ", missing " + this.stop, last.getLocation() );
				if( lastTuple != Tuple.EMPTY_TUPLE )
					if( lastTuple.size() == 1 )
						results.append( lastTuple.get( 0 ) );
					else
						results.append( lastTuple );
				if( results.size() == 0 )
					return null;
				return results;
			}
		}
	}

	// Parses one tuple (separated with ;)
	private Tuple parseTuple()
	{
		Tuple results = new Tuple();
		while( true )
		{
			Expression result = parseExpression();
			results.append( result ); // Can be null
			Token last = this.tokenizer.lastToken();
			if( last.getType() == TYPE.EOF || last.getType() == TYPE.SEMICOLON || last.getType() == this.stop )
			{
				if( this.stop != null && last.getType() == TYPE.EOF )
					throw new SourceException( "Unexpected " + last + ", missing " + this.stop, last.getLocation() );
				if( results.size() > 1 )
					return results;
				result = results.get( 0 );
				if( result == null )
					return Tuple.EMPTY_TUPLE;
				return results;
			}
			Assert.isTrue( last.getType() == TYPE.COMMA, "Not expecting token " + last );
		}
	}

	// Parses on expression (separated with commas)
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
				case COMMA:
					return result;

				case BINOP:
					Expression right = parseAtom();
					Assert.notNull( right );
					if( result instanceof Operation )
						result = ( (Operation)result ).append( token.getValue(), right );
					else
						result = Operation.operation( token.getValue(), result, right );
					break;

				case PAREN_OPEN:
					TYPE oldStop = swapStops( TYPE.PAREN_CLOSE );
					Expression parameters = parse();
					swapStops( oldStop );
					if( result instanceof Operation )
						result = ( (Operation)result ).append( token.getValue(), parameters );
					else
						result = Operation.operation( token.getValue(), result, parameters );
					break;

				case BRACKET_OPEN:
					oldStop = swapStops( TYPE.BRACKET_CLOSE );
					parameters = parse();
					swapStops( oldStop );
					if( result instanceof Operation )
						result = ( (Operation)result ).append( token.getValue(), parameters );
					else
						result = Operation.operation( token.getValue(), result, parameters );
					break;

				case UNAOP:
					if( result instanceof Operation )
						result = ( (Operation)result ).append( "@" + token.getValue(), null );
					else
						result = Operation.operation( "@" + token.getValue(), result, null );
					break;

				default:
					throw new SourceException( "Unexpected token '" + token + "'", token.getLocation() );
			}
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

			case NUMBER:
				return new NumberConstant( token.getLocation(), new BigDecimal( token.getValue() ) );

			case STRING:
				return parseString( token );

			case PAREN_OPEN:
				TYPE oldStop = swapStops( TYPE.PAREN_CLOSE );
				Expression result = parse();
				swapStops( oldStop );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE, "Not expecting token " + token );
				return result;

			case BRACE_OPEN:
				oldStop = swapStops( TYPE.BRACE_CLOSE );
				result = parse();
				swapStops( oldStop );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.BRACE_CLOSE, "Not expecting token " + token );
				return new Block( token.getLocation(), result );

			case BRACKET_OPEN:
				Token token2 = this.tokenizer.get();
				if( token2.eq( ":" ) )
				{
					token2 = this.tokenizer.get();
					Assert.isTrue( token2.getType() == TYPE.BRACKET_CLOSE, "Not expecting token " + token2 );
					return new EmptyMap( token.getLocation() );
				}
				else
					this.tokenizer.push();
				oldStop = swapStops( TYPE.BRACKET_CLOSE );
				result = parse();
				swapStops( oldStop );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.BRACKET_CLOSE, "Not expecting token " + token );
				return new solidstack.script.expressions.List( token.getLocation(), result );

			case BINOP:
				if( token.getValue().equals( "-" ) )
				{
					result = parseAtom(); // TODO Pre-apply
					return Operation.preOp( token.getLocation(), "-@", result );
				}
				if( token.getValue().equals( "+" ) )
					return parseAtom(); // TODO Is this correct, just ignore the operation?
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
						throw new SourceException( "Expected an opening parenthesis", token2.getLocation() );
					oldStop = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStop );
					if( expressions.size() != 2 && expressions.size() != 3 )
						throw new SourceException( "Expected 2 or 3 expressions", token2.getLocation() );
					return new If( token.getLocation(), expressions.get( 0 ), expressions.get( 1 ), expressions.size() == 3 ? expressions.get( 2 ) : null );
				}

				if( token.getValue().equals( "while" ) )
				{
					token2 = this.tokenizer.get();
					if( token2.getType() != TYPE.PAREN_OPEN )
						throw new SourceException( "Expected an opening parenthesis", token2.getLocation() );
					oldStop = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStop );
					if( expressions.size() < 2 ) // TODO And 1?
						throw new SourceException( "Expected at least 2 expressions", token2.getLocation() );
					return new While( token.getLocation(), expressions.remove( 0 ), expressions );
				}

				if( token.getValue().equals( "fun" ) )
				{
					token2 = this.tokenizer.get();
					if( token2.getType() != TYPE.PAREN_OPEN && token2.getType() != TYPE.BRACE_OPEN )
						throw new SourceException( "Expected one of (, {", token2.getLocation() );
					if( token2.getType() == TYPE.PAREN_OPEN )
						oldStop = swapStops( TYPE.PAREN_CLOSE );
					else
						oldStop = swapStops( TYPE.BRACE_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStop );
					if( expressions.size() < 2 ) // TODO And 1?
						throw new SourceException( "Expected 2 or more expressions", token2.getLocation() );
					List<String> parameters = new ArrayList<String>();
					Expression pars = expressions.remove( 0 );
					if( pars instanceof Tuple )
					{
						for( Expression par : ( (Tuple)pars ).getExpressions() )
						{
							if( !( par instanceof Identifier ) )
								throw new SourceException( "Expected an identifier", token2.getLocation() ); // FIXME Use the line number from the par
							parameters.add( ( (Identifier)par ).getName() );
						}
					}
					else if( pars != null )
					{
						if( !( pars instanceof Identifier ) )
							throw new SourceException( "Expected an identifier", token2.getLocation() ); // FIXME Use the line number from the par
						parameters.add( ( (Identifier)pars ).getName() );
					}
					return new Function( token.getLocation(), parameters, expressions, token2.getType() == TYPE.BRACE_OPEN );
				}

				return new Identifier( token.getLocation(), token.getValue() );

			case UNAOP:
				result = parseAtom();
				return Operation.preOp( token.getLocation(), token.getValue() + "@", result ); // TODO Pre-apply

			default:
				throw new SourceException( "Unexpected token " + token, token.getLocation() );
		}
	}

	/**
	 * Parses a super string.
	 *
	 * @param s The super string to parse.
	 * @return An expression.
	 */
	private Expression parseString( Token string )
	{
		SourceReader in = SourceReaders.forString( string.getValue(), string.getLocation() );
		StringTokenizer t = new StringTokenizer( in );
		ScriptTokenizer oldTokenizer = this.tokenizer;
		this.tokenizer = t;

		TYPE oldStop = swapStops( TYPE.BRACE_CLOSE );

		StringExpression result = new StringExpression( string.getLocation() );

		String fragment = t.getFragment();
		while( t.foundExpression() )
		{
			if( fragment.length() != 0 )
				result.append( new StringConstant( string.getLocation(), fragment ) );
			Expressions expressions = parse();
			result.append( expressions );
			fragment = t.getFragment();
		}
		if( fragment.length() != 0 )
			result.append( new StringConstant( string.getLocation(), fragment ) );

		swapStops( oldStop );

		this.tokenizer = oldTokenizer;

		if( result.size() == 0 )
			return new StringConstant( string.getLocation(), "" );
		if( result.size() == 1 && result.get( 0 ) instanceof StringConstant )
			return result.get( 0 );
		return result;
	}

	private TYPE swapStops( TYPE stop )
	{
		TYPE result = this.stop;
		this.stop = stop;
		return result;
	}
}
