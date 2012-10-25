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

import solidstack.io.PushbackReader;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.script.ScriptTokenizer.Token.TYPE;


/**
 * This is a tokenizer for CSV. It maintains the current line number, and it ignores whitespace.
 *
 * @author René M. de Bloois
 */
public class ScriptTokenizer
{
	/**
	 * The reader used to read from and push back characters.
	 */
	protected PushbackReader in;

	/**
	 * Buffer for the result.
	 */
	protected StringBuilder result = new StringBuilder( 256 );

	protected Token last;

	protected boolean pushed;


	/**
	 * Constructs a new instance of the Tokenizer.
	 *
	 * @param in The input.
	 */
	public ScriptTokenizer( SourceReader in )
	{
		this.in = new PushbackReader( in );
	}

	/**
	 * Returns the next token from the input.
	 *
	 * @return A token from the input. Null if there are no more tokens available.
	 */
	public Token get()
	{
		if( this.pushed )
		{
			this.pushed = false;
			return lastToken();
		}
		return this.last = get0();
	}

	public Token lastToken()
	{
		if( this.pushed )
			throw new IllegalStateException( "Token has been pushed back" );
		if( this.last == null )
			throw new IllegalStateException( "There is no last token" );
		return this.last;
	}

	public void push()
	{
		if( this.pushed )
			throw new IllegalStateException( "Token has already been pushed back" );
		this.pushed = true;
	}

	private Token get0()
	{
		StringBuilder result = this.result;
		result.setLength( 0 );

		while( true )
		{
			int ch = this.in.read();
			if( ch == -1 )
				return Token.EOF;

			switch( ch )
			{
				// Whitespace
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					continue;

				case 'a': case 'b': case 'c': case 'd': case 'e':
				case 'f': case 'g': case 'h': case 'i': case 'j':
				case 'k': case 'l': case 'm': case 'n': case 'o':
				case 'p': case 'q': case 'r': case 's': case 't':
				case 'u': case 'v': case 'w': case 'x': case 'y':
				case 'z':
				case 'A': case 'B': case 'C': case 'D': case 'E':
				case 'F': case 'G': case 'H': case 'I': case 'J':
				case 'K': case 'L': case 'M': case 'N': case 'O':
				case 'P': case 'Q': case 'R': case 'S': case 'T':
				case 'U': case 'V': case 'W': case 'X': case 'Y':
				case 'Z':
				case '_': case '$':
					while( ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '$' || ch == '_' )
					{
						result.append( (char)ch );
						ch = this.in.read();
					}
					this.in.push( ch );
					// TODO Internalize identifier tokens
					return new Token( Token.TYPE.IDENTIFIER, result.toString() );

				case '"':
					while( true )
					{
						ch = this.in.read();
						if( ch == -1 )
							throw new SourceException( "Missing \"", this.in.getLocation() );
						if( ch == '"' )
							return new Token( TYPE.STRING, result.toString() );
						if( ch == '\\' )
						{
							ch = this.in.read();
							if( ch == -1 )
								throw new SourceException( "Incomplete escape sequence", this.in.getLocation() );
							switch( ch )
							{
								case 'b': ch = '\b'; break;
								case 'f': ch = '\f'; break;
								case 'n': ch = '\n'; break;
								case 'r': ch = '\r'; break;
								case 't': ch = '\t'; break;
								case '\"': break;
								case '\\': break;
								case '$': result.append( '\\' ); break; // TODO Remember, not for '' strings
								case 'u':
									char[] codePoint = new char[ 4 ];
									for( int i = 0; i < 4; i++ )
									{
										ch = this.in.read();
										codePoint[ i ] = (char)ch;
										if( !( ch >= '0' && ch <= '9' ) )
											throw new SourceException( "Illegal escape sequence: \\u" + String.valueOf( codePoint, 0, i + 1 ), this.in.getLocation() );
									}
									ch = Integer.valueOf( String.valueOf( codePoint ), 16 );
									break;
								default:
									throw new SourceException( "Illegal escape sequence: \\" + ( ch >= 0 ? (char)ch : "" ), this.in.getLocation() );
							}
						}
						result.append( (char)ch );
					}

				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					while( ch >= '0' && ch <= '9' )
					{
						result.append( (char)ch );
						ch = this.in.read();
					}
					if( ch == '.' )
					{
						result.append( (char)ch );
						ch = this.in.read();
						if( !( ch >= '0' && ch <= '9' ) )
						{
							this.in.push( ch );
							this.in.push( '.' );
							return new Token( TYPE.NUMBER, result.toString() );
						}
						while( ch >= '0' && ch <= '9' )
						{
							result.append( (char)ch );
							ch = this.in.read();
						}
					}
					if( ch == 'E' || ch == 'e' )
					{
						result.append( (char)ch );
						ch = this.in.read();
						if( ch == '+' || ch == '-' )
						{
							result.append( (char)ch );
							ch = this.in.read();
						}
						if( !( ch >= '0' && ch <= '9' ) )
							throw new SourceException( "Invalid number", this.in.getLocation() );
						while( ch >= '0' && ch <= '9' )
						{
							result.append( (char)ch );
							ch = this.in.read();
						}
					}
					this.in.push( ch );
					return new Token( TYPE.NUMBER, result.toString() );

				case '+':
				case '-':
					int ch2 = this.in.read();
					if( ch2 == ch )
						return new Token( Token.TYPE.UNAOP, String.valueOf( new char[] { (char)ch, (char)ch } ) );
					if( ch == '-' && ch2 == '>' )
						return Token.LAMBDA;
					this.in.push( ch2 );
					//$FALL-THROUGH$
				case '*':
				case '/':
				case '?':
				case '.':
					return new Token( Token.TYPE.BINOP, String.valueOf( (char)ch ) );
				case ':':
					return Token.COLON;

				case '!':
					return new Token( Token.TYPE.UNAOP, "!" );

				case '<':
				case '>':
					return new Token( Token.TYPE.BINOP, String.valueOf( (char)ch ) );

				case '=':
					ch = this.in.read();
					if( ch == '=' )
						return new Token( Token.TYPE.BINOP, "==" );
					this.in.push( ch );
					return new Token( Token.TYPE.BINOP, "=" ); // TODO Predefine all operator tokens

				case '&':
					ch = this.in.read();
					if( ch == '&' )
						return new Token( Token.TYPE.BINOP, "&&" );
					this.in.push( ch );
					throw new SourceException( "Unexpected character '" + (char)ch + "'", this.in.getLocation() );

				case '|':
					ch = this.in.read();
					if( ch == '|' )
						return new Token( Token.TYPE.BINOP, "||" );
					this.in.push( ch );
					throw new SourceException( "Unexpected character '" + (char)ch + "'", this.in.getLocation() );

				case '(':
					return Token.PAREN_OPEN;
				case ')':
					return Token.PAREN_CLOSE;
				case ',':
					return Token.COMMA;
				case ';':
					return Token.SEMICOLON;
				case '{':
					return Token.BRACE_OPEN;
				case '}':
					return Token.BRACE_CLOSE;

				default:
					throw new SourceException( "Unexpected character '" + (char)ch + "'", this.in.getLocation() );
			}
		}
	}

	/**
	 * Returns the current line number.
	 *
	 * @return The current line number.
	 */
	public int getLineNumber()
	{
		return this.in.getLineNumber();
	}

	/**
	 * Returns the current file location.
	 *
	 * @return The current file location.
	 */
	public SourceLocation getLocation()
	{
		return this.in.getLocation();
	}

	/**
	 * Returns the underlying reader. But only if the back buffer is empty, otherwise an IllegalStateException is thrown.
	 *
	 * @return The underlying reader.
	 */
	public SourceReader getReader()
	{
		return this.in.getReader();
	}


	/**
	 * A CSV token.
	 *
	 * @author René M. de Bloois
	 */
	// TODO Maybe we should remove this token class, and introduce the even mechanism like in JSONParser.
	static public class Token
	{
		static public enum TYPE { IDENTIFIER, NUMBER, STRING, BINOP, UNAOP, PAREN_OPEN, PAREN_CLOSE, BRACE_OPEN, BRACE_CLOSE, COMMA, SEMICOLON, COLON, LAMBDA, NULL, EOF }

		static final protected Token PAREN_OPEN = new Token( TYPE.PAREN_OPEN, "(" );
		static final protected Token PAREN_CLOSE = new Token( TYPE.PAREN_CLOSE, ")" );
		static final protected Token BRACE_OPEN = new Token( TYPE.BRACE_OPEN, "{" );
		static final protected Token BRACE_CLOSE = new Token( TYPE.BRACE_CLOSE, "}" );
		static final protected Token COMMA = new Token( TYPE.COMMA, "," );
		static final protected Token SEMICOLON = new Token( TYPE.SEMICOLON, ";" );
		static final protected Token COLON = new Token( TYPE.COLON, ":" );
		static final protected Token LAMBDA = new Token( TYPE.LAMBDA, "->" );
		static final protected Token NULL = new Token( TYPE.NULL );
		static final protected Token EOF = new Token( TYPE.EOF );

		/**
		 * The type of the token.
		 */
		private TYPE type;

		/**
		 * The value of the token.
		 */
		private String value;

		/**
		 * Constructs a new token.
		 *
		 * @param type The type of the token.
		 */
		private Token( TYPE type )
		{
			this.type = type;
		}

		protected Token( TYPE type, String value )
		{
			this.type = type;
			this.value = value;
		}

		public TYPE getType()
		{
			return this.type;
		}

		/**
		 * Returns the value of token.
		 *
		 * @return The value of token.
		 */
		public String getValue()
		{
			if( this.type == TYPE.NULL )
				return null;
			if( this.value == null )
				throw new IllegalStateException( "Value is null" );
			return this.value;
		}

		@Override
		public String toString()
		{
			if( this.value != null )
				return this.value.toString();
			if( this.type == TYPE.EOF )
				return "EOF";
			return this.type.toString(); // TODO Is this correct?
		}

		public boolean eq( String s )
		{
			if( this.value == null )
				return false;
			return this.value.equals( s );
		}
	}

	public void close()
	{
		this.in.close();
	}
}
