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
	private TYPE stop2;


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
		Tuple last = null;
		while( true )
		{
			if( last != null )
				if( last == Tuple.EMPTY_TUPLE )
					results.append( null );
				else if( last.size() == 1 )
					results.append( last.get( 0 ) );
				else
					results.append( last );
			last = parseTuple();
			Assert.isTrue( last != null  );
			Token lastToken = this.tokenizer.lastToken();
			if( lastToken == Token.TOK_EOF || lastToken.getType() == this.stop ) // TODO stop2?
			{
				if( last != Tuple.EMPTY_TUPLE )
					if( last.size() == 1 )
						results.append( last.get( 0 ) );
					else
						results.append( last );
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
			if( last == Token.TOK_EOF || last == Token.TOK_SEMICOLON || last.getType() == this.stop )
			{
				if( results.size() > 1 )
					return results;
				result = results.get( 0 );
				if( result == null )
					return Tuple.EMPTY_TUPLE;
				return results;
			}
			Assert.isTrue( last == Token.TOK_COMMA, "Not expecting token " + last );
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
			if( type == this.stop || type == this.stop2 )
				return result;

			switch( type )
			{
				case EOF:
				case SEMICOLON:
				case COMMA:
					return result;

				case BINOP:
					if( token.getValue().equals( "?" ) )
					{
						TYPE old = this.stop;
						this.stop = TYPE.COLON;
						TYPE old2 = this.stop2;
						this.stop2 = null;
						Expression middle = parse();
						this.stop = old;
						this.stop2 = old2;
						Expression right = parseAtom();
						Assert.notNull( right );
						if( result instanceof Operation )
							result = ( (Operation)result ).append( middle, right );
						else
							result = Operation.operation( result, middle, right );
					}
					else
					{
						Expression right = parseAtom();
						Assert.notNull( right );
						if( result instanceof Operation )
							result = ( (Operation)result ).append( token.getValue(), right );
						else
							result = Operation.operation( token.getValue(), result, right );
					}
					break;

				case PAREN_OPEN:
					TYPE old = this.stop;
					this.stop = TYPE.PAREN_CLOSE;
					TYPE old2 = this.stop2;
					this.stop2 = null;
					Expression parameters = parse();
					this.stop = old;
					this.stop2 = old2;
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
					throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
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
		if( type == this.stop ) // TODO stop2?
			return null;
		switch( type )
		{
			case EOF:
			case SEMICOLON:
				return null;

			case NUMBER:
				return new NumberConstant( new BigDecimal( token.getValue() ) );

			case STRING:
				return parseString( token.getValue() );

			case PAREN_OPEN:
				TYPE[] oldStops = swapStops( TYPE.PAREN_CLOSE );
				Expression result = parse();
				swapStops( oldStops );
				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE, "Not expecting token " + token );
				return result;

			case BINOP:
				if( token.getValue().equals( "-" ) )
				{
					result = parseAtom(); // TODO Pre-apply
					return Operation.operation( "-@", null, result );
				}
				if( token.getValue().equals( "+" ) )
					return parseAtom(); // TODO Is this correct, just ignore the operation?
				throw new SourceException( "Unexpected token " + token, this.tokenizer.getLocation() );

			case IDENTIFIER:
				String name = token.getValue();
				if( name.equals( "false" ) )
					return new BooleanConstant( false );

				if( name.equals( "true" ) )
					return new BooleanConstant( true );

				if( name.equals( "null" ) )
					return new NullConstant();

				if( token.getValue().equals( "if" ) )
				{
					token = this.tokenizer.get();
					if( token.getType() != TYPE.PAREN_OPEN )
						throw new SourceException( "Expected an opening parenthesis", this.tokenizer.getLocation() );
					oldStops = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStops );
					if( expressions.size() != 2 && expressions.size() != 3 )
						throw new SourceException( "Expected 2 or 3 expressions", this.tokenizer.getLocation() );
					return new If( expressions.get( 0 ), expressions.get( 1 ), expressions.size() == 3 ? expressions.get( 2 ) : null );
				}

				if( token.getValue().equals( "while" ) )
				{
					token = this.tokenizer.get();
					if( token.getType() != TYPE.PAREN_OPEN )
						throw new SourceException( "Expected an opening parenthesis", this.tokenizer.getLocation() );
					oldStops = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStops );
					if( expressions.size() < 2 ) // TODO And 1?
						throw new SourceException( "Expected at least 2 expressions", this.tokenizer.getLocation() );
					return new While( expressions.remove( 0 ), expressions );
				}

				if( token.getValue().equals( "fun" ) )
				{
					token = this.tokenizer.get();
					if( token.getType() != TYPE.PAREN_OPEN )
						throw new SourceException( "Expected an opening parenthesis", this.tokenizer.getLocation() );
					oldStops = swapStops( TYPE.PAREN_CLOSE );
					Expressions expressions = parse();
					swapStops( oldStops );
					if( expressions.size() < 2 ) // TODO And 1?
						throw new SourceException( "Expected 2 or more expressions", this.tokenizer.getLocation() );
					List<String> parameters = new ArrayList<String>();
					Expression pars = expressions.remove( 0 );
					if( pars instanceof Tuple )
					{
						for( Expression par : ( (Tuple)pars ).getExpressions() )
						{
							if( !( par instanceof Identifier ) )
								throw new SourceException( "Expected an identifier", this.tokenizer.getLocation() );
							parameters.add( ( (Identifier)par ).getName() );
						}
					}
					else if( pars != null )
					{
						if( !( pars instanceof Identifier ) )
							throw new SourceException( "Expected an identifier", this.tokenizer.getLocation() );
						parameters.add( ( (Identifier)pars ).getName() );
					}
					return new Function( parameters, expressions );
				}

				return new Identifier( token.getValue() );

			case UNAOP:
				result = parseAtom();
				return Operation.operation( token.getValue() + "@", null, result ); // TODO Pre-apply

			default:
				throw new SourceException( "Unexpected token " + token, this.tokenizer.getLocation() );
		}
	}

	/**
	 * Parses a super string.
	 *
	 * @param s The super string to parse.
	 * @return An expression.
	 */
	private Expression parseString( String s )
	{
		SourceReader in = SourceReaders.forString( s, this.tokenizer.getLocation() ); // TODO This location is not correct
		StringTokenizer t = new StringTokenizer( in );
		ScriptTokenizer oldTokenizer = this.tokenizer;
		this.tokenizer = t;

		TYPE[] oldStops = swapStops( TYPE.BRACE_CLOSE );

		StringExpression result = new StringExpression();

		String fragment = t.getFragment();
		while( t.foundExpression() )
		{
			if( fragment.length() != 0 )
				result.append( new StringConstant( fragment ) );
			Expressions expressions = parse();
			result.append( expressions );
			fragment = t.getFragment();
		}
		if( fragment.length() != 0 )
			result.append( new StringConstant( fragment ) );

		swapStops( oldStops );

		this.tokenizer = oldTokenizer;

		if( result.size() == 1 && result.get( 0 ) instanceof StringConstant )
			return result.get( 0 );
		return result;
	}

	private TYPE[] swapStops( TYPE... stop )
	{
		TYPE[] result = new TYPE[] { this.stop, this.stop2 };
		this.stop = stop[ 0 ];
		this.stop2 = stop.length == 2 ? stop[ 1 ] : null;
		return result;
	}

//			else if( token.getType() == TYPE.LAMBDA )
//			{
//				Assert.isTrue( result != null );
//				Expression right = parseOne( false, null );
//				if( right == null )
//					throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
//				if( result instanceof Operation )
//					result = ( (Operation)result ).append( token.getValue(), right );
//				else
//					result = Operation.operation( token.getValue(), result, right );
//			}
}
