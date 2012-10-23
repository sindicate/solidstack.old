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
import solidstack.lang.Assert;
import solidstack.script.ScriptTokenizer.Token;
import solidstack.script.ScriptTokenizer.Token.TYPE;

public class ScriptParser
{
	private ScriptTokenizer tokenizer;
	private TYPE stop;
	private TYPE stop2;

	public ScriptParser( ScriptTokenizer t )
	{
		this.tokenizer = t;
	}

	// Parses everything
	public Expressions parse()
	{
		Expressions results = new Expressions();
		while( true )
		{
			Expression result = parseTuple();
			if( result != null )
				results.append( result );
			Token lastToken = this.tokenizer.lastToken();
			if( lastToken == Token.EOF || lastToken.getType() == this.stop )
				return results;
		}
	}

	// Parses one expression (separated with ;s)
	public Expression parseTuple()
	{
		Tuple results = new Tuple();
		while( true )
		{
			Expression result = parseExpression();
			results.append( result ); // Can be null
			Token last = this.tokenizer.lastToken();
			if( last.getType() == this.stop )
				return results;
			if( last == Token.EOF || last == Token.SEMICOLON )
			{
				if( results.onlyOneNull() )
					return null;
				return results;
			}
			Assert.isTrue( last == Token.COMMA, "Not expecting token " + last );
		}
	}

	// Parses on expression (separated with commas)
	public Expression parseExpression()
	{
		Expression result = parseAtom();
		if( result == null )
			return null;

		while( true )
		{
			Token token = this.tokenizer.get();
			if( token.getType() == TYPE.EOF )
				return result;
			if( token.getType() == TYPE.SEMICOLON )
				return result;
			if( token.getType() == this.stop )
				return result;
			if( token.getType() == this.stop2 )
				return result;
			if( token.getType() == TYPE.BINOP )
			{
				if( token.getValue().equals( "?" ) )
				{
					TYPE old = this.stop;
					this.stop = TYPE.COLON;
					Expression middle = parse();
					this.stop = old;
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
			}
			else if( token.getType() == TYPE.PAREN_OPEN )
			{
				TYPE old = this.stop;
				this.stop = TYPE.PAREN_CLOSE;
				TYPE old2 = this.stop2;
				this.stop2 = TYPE.COMMA;
				Expression parameters = parse();
				this.stop = old;
				this.stop2 = old2;
				if( result instanceof Operation )
					result = ( (Operation)result ).append( token.getValue(), parameters );
				else
					result = Operation.operation( token.getValue(), result, parameters );
			}
			else
				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
		}

//		Token token = this.tokenizer.get();
//		Assert.isTrue( token.getType() == TYPE.EOF );
//		return result;
	}

	public Expression parseAtom()
	{
		Token token = this.tokenizer.get();
		if( token == Token.EOF )
			return null;
		if( token.getType() == TYPE.SEMICOLON )
			return null;
		if( token.getType() == TYPE.NUMBER )
			return new NumberConstant( new BigDecimal( token.getValue() ) );
		if( token.getType() == TYPE.STRING )
			return new StringConstant( token.getValue() );
		if( token == Token.PAREN_OPEN )
		{
			TYPE old = this.stop;
			this.stop = TYPE.PAREN_CLOSE;
			Expression result = parse();
			this.stop = old;
			Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE, "Not expecting token " + token );
			return result;
		}
		if( token.getType() == TYPE.BINOP )
		{
			if( token.getValue().equals( "-" ) )
			{
				Expression result = parseAtom(); // TODO Pre-apply
				return Operation.operation( "-@", null, result );
			}
			else if( token.getValue().equals( "+" ) )
			{
				return parseAtom(); // TODO Is this correct, just ignore the operation?
			}
		}
		else if( token.getType() == TYPE.IDENTIFIER )
		{
			String name = token.getValue();
			if( name.equals( "false" ) )
				return new BooleanConstant( false );
			if( name.equals( "true" ) )
				return new BooleanConstant( true );
			if( name.equals( "null" ) )
				return new NullConstant();
//			return new Identifier( token.getValue() );
		}
		else if( token.getType() == TYPE.UNAOP )
		{
			if( token.getValue().equals( "!" ) )
			{
				Expression result = parseAtom(); // TODO Pre-apply
				return Operation.operation( "!@", null, result );
			}
//			Expression result = parseOne( false, null );
//			return Operation.operation( token.getValue() + "@", null, result );
		}
		Assert.isTrue( token.getType() == TYPE.IDENTIFIER, "Not expecting token " + token );
		return new Identifier( token.getValue() );
	}

//	public Expression parse( String stop, String stop2 )
//	{
//		Expressions results = new Expressions();
//		while( true )
//		{
//			Expression result = parseExpression( stop, stop2 );
//			if( result != null )
//				results.append( result );
//			Token last = this.tokenizer.lastToken();
//			if( last == Token.EOF || last.getValue().equals( stop ) || last.getValue().equals( stop2 ) )
//				return results.normalize();
//		}
//	}
//
//	public Expression parseExpression( String stop, String stop2 )
//	{
//		Expression result = parseOne( true, stop2 );
//		if( result == null )
//			return null;
//
//		while( true )
//		{
//			Token token = this.tokenizer.get();
//			if( stop == null && stop2 == null )
//			{
//				if( token.getType() == TYPE.EOF )
//					return result;
//			}
//			else if( token.eq( stop ) || token.eq( stop2 ) )
//				return result;
//			if( token.getType() == TYPE.SEMICOLON )
//				return result;
//			if( token.getType() == TYPE.PAREN_OPEN )
//			{
//				Assert.isTrue( result != null );
//				Tuple parameters = new Tuple();
//				do
//				{
//					Expression parameter = parse( ",", ")" );
//					if( parameter != null )
//						parameters.append( parameter );
//				}
//				while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
//				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
//				if( result instanceof Operation )
//					result = ( (Operation)result ).append( token.getValue(), parameters );
//				else
//					result = Operation.operation( token.getValue(), result, parameters );
//			}
//			else if( token.getType() == TYPE.BINOP )
//			{
//				Assert.isTrue( result != null );
//				if( token.getValue().equals( "?" ) )
//				{
//					Expression first = parse( ":", null );
//					Expression second = parseOne( false, null );
//					if( second == null )
//						throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
//					if( result instanceof Operation )
//						result = ( (Operation)result ).append( first, second );
//					else
//						result = Operation.operation( result, first, second );
//				}
//				else
//				{
//					Assert.isFalse( token.getValue().equals( ":" ) );
//					Expression right = parseOne( false, null );
//					if( right == null )
//						throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
//					if( result instanceof Operation )
//						result = ( (Operation)result ).append( token.getValue(), right );
//					else
//						result = Operation.operation( token.getValue(), result, right );
//				}
//			}
//			else if( token.getType() == TYPE.UNAOP )
//			{
//				Assert.isTrue( result != null );
//				if( result instanceof Operation )
//					result = ( (Operation)result ).append( "@" + token.getValue(), null );
//				else
//					result = Operation.operation( "@" + token.getValue(), result, null );
//			}
//			else if( token.getType() == TYPE.DOT )
//			{
//				// TODO This is equal to the binary operation
//				Expression right = parseOne( false, null );
//				if( right == null )
//					throw new SourceException( "Unexpected token '" + this.tokenizer.lastToken() + "'", this.tokenizer.getLocation() );
//				if( result instanceof Operation )
//					result = ( (Operation)result ).append( token.getValue(), right );
//				else
//					result = Operation.operation( token.getValue(), result, right );
//			}
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
//			else
//				throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
//		}
//	}
//
//	public Expression parseOne( boolean start, String or )
//	{
//		Token token = this.tokenizer.get();
//
//		if( start )
//		{
//			while( token.getType() == TYPE.SEMICOLON )
//				token = this.tokenizer.get();
//			if( token == Token.EOF || token.eq( or ) )
//				return null;
//		}
//
//		if( token.getType() == TYPE.IDENTIFIER )
//		{
//			String name = token.getValue();
//			if( name.equals( "false" ) )
//				return new BooleanConstant( false );
//			if( name.equals( "true" ) )
//				return new BooleanConstant( true );
//			if( name.equals( "null" ) )
//				return new NullConstant();
//			if( token.getValue().equals( "if" ) )
//			{
//				Expression result = parseOne( true, null );
//				if( !( result instanceof Tuple ) )
//					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
//				Expression left = parseOne( true, null );
//				token = this.tokenizer.get();
//				if( token.getType() != TYPE.IDENTIFIER || !token.getValue().equals( "else" ) )
//				{
//					this.tokenizer.push();
//					return new If( result, left, null );
//				}
//				Expression right = parseOne( true, null );
//				return new If( result, left, right );
//			}
//			if( token.getValue().equals( "while" ) )
//			{
//				Expression result = parseOne( false, null );
//				if( !( result instanceof Tuple ) )
//					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
//				Expression left = parseExpression( null, null );
//				return new While( result, left );
//			}
//			if( token.getValue().equals( "function" ) )
//			{
//				token = this.tokenizer.get();
//				if( token.getType() != TYPE.PAREN_OPEN )
//					throw new SourceException( "Expected a parenthesis (", this.tokenizer.getLocation() );
//				List<String> parameters = new ArrayList<String>();
//				do
//				{
//					Expression parameter = parse( ",", ")" );
//					if( parameter != null )
//					{
//						if( !( parameter instanceof Identifier ) )
//							throw new SourceException( "Expected an identifier", this.tokenizer.getLocation() );
//						parameters.add( ( (Identifier)parameter ).getName() );
//					}
//				}
//				while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
//				Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
//				Expression block = parseOne( true, null );
//				return new Function( parameters, block );
//			}
//			return new Identifier( token.getValue() );
//		}
//		if( token.getType() == TYPE.NUMBER )
//			return new NumberConstant( new BigDecimal( token.getValue() ) );
//		if( token.getType() == TYPE.STRING )
//			return new StringConstant( token.getValue() );
//		if( token == Token.PAREN_OPEN )
//		{
//			Tuple tuple = new Tuple();
//			do
//			{
//				Expression element = parse( ",", ")" );
//				if( element != null )
//					tuple.append( element );
//			}
//			while( this.tokenizer.lastToken().getType() == TYPE.COMMA );
//			Assert.isTrue( this.tokenizer.lastToken().getType() == TYPE.PAREN_CLOSE ); // TODO Not really needed
//			return tuple;
//		}
//		if( token.getType() == TYPE.BINOP )
//			if( token.getValue().equals( "-" ) )
//			{
//				Expression result = parseOne( false, null );
//				if( result instanceof NumberConstant )
//					return ( (NumberConstant)result ).negate();
//				return Operation.operation( "-@", result, null );
//			}
//			else if( token.getValue().equals( "+" ) )
//			{
//				return parseOne( false, null );
//			}
//		if( token.getType() == TYPE.UNAOP )
//		{
//			if( token.getValue().equals( "!" ) )
//			{
//				Expression result = parseOne( false, null );
//				if( result instanceof BooleanConstant )
//					return ( (BooleanConstant)result ).not();
//				return Operation.operation( "!@", null, result );
//			}
//			Expression result = parseOne( false, null );
//			return Operation.operation( token.getValue() + "@", null, result );
//		}
////		if( token.getType() == TYPE.EOF )
////			return null;
//		if( token.getType() == TYPE.BRACE_OPEN )
//			return parseBlock();
//		throw new SourceException( "Unexpected token '" + token + "'", this.tokenizer.getLocation() );
//	}
//
//	public Expression parseBlock()
//	{
//		Expression result = parse( null, "}" );
//		if( !( result instanceof Expressions ) )
//			return new Expressions( result );
//		return result;
//
//	}
}
