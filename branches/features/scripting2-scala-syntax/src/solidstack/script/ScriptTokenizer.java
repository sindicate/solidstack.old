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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.io.PushbackReader;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.lang.Assert;


/**
 * This is a tokenizer for CSV. It maintains the current line number, and it ignores whitespace.
 *
 * @author René M. de Bloois
 */
public class ScriptTokenizer
{
	/**
	 * Reserved words.
	 */
	@SuppressWarnings( "javadoc" )
	static public enum TOKENTYPE {
		// Literals & identifiers
		INTEGER, DECIMAL, STRING, IDENTIFIER, OPERATOR,
		// Fixed characters
		PAREN_OPEN( "(", false ), PAREN_CLOSE( ")", false ), BRACKET_OPEN( "[", false ), BRACKET_CLOSE( "]", false ), BRACE_OPEN( "{", false ), BRACE_CLOSE( "}", false ),
		BACKQUOTE( "`", false ), QUOTE( "'", false ), DOT( ".", false ), SEMICOLON( ";", false ), COMMA( ",", false ),
		EOF,
		// Reserved words
		ABSTRACT( "abstract" ), CASE( "case" ), CATCH( "catch" ), /* CLASS( "class" ), */
		DEF( "def" ), DO( "do" ), ELSE( "else" ), EXTENDS( "extends" ),
		FALSE( "false" ), FINAL( "final" ), FINALLY( "finally" ), FOR( "for" ),
		FORSOME( "forSome" ), IF( "if" ), IMPLICIT( "implicit" ), IMPORT( "import" ),
		LAZY( "lazy" ), MATCH( "match" ), NEW( "new" ), NULL( "null" ),
		OBJECT( "object" ), OVERRIDE( "override" ), PACKAGE( "package" ), PRIVATE( "private" ),
		PROTECTED( "protected" ), RETURN( "return" ), SEALED( "sealed" ), SUPER( "super" ),
		THIS( "this" ), THROW( "throw" ), TRAIT( "trait" ), TRY( "try" ),
		TRUE( "true" ), TYPE( "type" ), VAL( "val" ), VAR( "var" ),
		WHILE( "while" ), WITH( "with" ), YIELD( "yield" ),
		UNDERSCORE( "_" ), COLON( ":" ), EQUALS( "=" ), HASH( "#" ), AT( "@" ),
		FUNCTION( "=>" ), GENERATOR( "<-" ), UPPERBOUND( "<:" ), VIEWBOUND( "<%" ), LOWERBOUND( ">:" );
		public final String word;
		public final boolean reserved;
		private TOKENTYPE() { this( null, false ); }
		private TOKENTYPE( String word ) { this( word, true ); }
		private TOKENTYPE( String word, boolean reserved ) { this.word = word; this.reserved = reserved; }
	}

	/**
	 * Map of reserved words.
	 */
	static public final Map<String, TOKENTYPE> RESERVED_WORDS;

	static
	{
		RESERVED_WORDS = new HashMap<String, TOKENTYPE>();
		for( TOKENTYPE type : TOKENTYPE.values() )
			if( type.reserved )
				RESERVED_WORDS.put( type.word, type );
	}

	/**
	 * The reader used to read from and push back characters.
	 */
	private PushbackReader in;

	/**
	 * Buffer for the result.
	 */
	private StringBuilder buffer = new StringBuilder( 256 );

	/**
	 * The last token read.
	 */
	private Token last;

	private List<Token> window = new ArrayList<Token>();
	private int pos;


	/**
	 * Constructs a new instance of the Tokenizer.
	 *
	 * @param in The input.
	 */
	public ScriptTokenizer( SourceReader in )
	{
		this.in = new PushbackReader( in );
		this.window.add( null );
		this.window.add( null );
		this.window.add( null );
		this.pos = 3;
	}

	/**
	 * @return The underlying reader.
	 */
	public PushbackReader getIn()
	{
		return this.in;
	}

	/**
	 * Clears the buffer and returns it.
	 *
	 * @return The cleared buffer.
	 */
	protected StringBuilder clearBuffer()
	{
		StringBuilder buffer = this.buffer;
		buffer.setLength( 0 );
		return buffer;
	}

	/**
	 * Returns the next token from the input.
	 *
	 * @return A token from the input. Null if there are no more tokens available.
	 */
	public Token get()
	{
		if( this.pos == 3 )
		{
			this.window.remove( 0 );
			Token token = get0();
			this.window.add( token );
			return token;
		}
		return this.window.get( this.pos++ );
	}

	/**
	 * @return The last token read.
	 */
	public Token last()
	{
		if( this.pos == 0 )
			throw new IllegalStateException( "There is no last event" );
		Token result = this.window.get( this.pos - 1 );
		if( result == null )
			throw new IllegalStateException( "The event has not been retrieved yet" );
		return result;
	}

	/**
	 * Push the token back.
	 */
	public void push()
	{
		if( this.pos == 0 )
			throw new IllegalStateException( "Can't push further" );
		this.pos--;
	}

	private Token get0()
	{
		StringBuilder result = clearBuffer();
		PushbackReader in = getIn();

		while( true )
		{
			int ch;

			ws: while( true )
				switch( ch = in.read() )
				{
					case -1: return new Token( TOKENTYPE.EOF, in.getLocation(), null );
					default: break ws;
					case ' ': case '\t': case '\n': case '\r': // Whitespace
				}

			SourceLocation location = in.getLocation();

			switch( ch )
			{
				// Identifier
				case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j':
				case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't':
				case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J':
				case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T':
				case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
				case '_': case '$': // Identifier
					do
					{
						result.append( (char)ch );
						ch = in.read();
					}
					while( ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '$' || ch == '_' );
					in.push( ch );
					String value = result.toString();
					TOKENTYPE type = RESERVED_WORDS.get( value );
					if( type != null )
						return new Token( type, location, value );
					return new Token( TOKENTYPE.IDENTIFIER, location, value );

				// String
				case '"':
					while( true )
					{
						switch( ch = in.read() )
						{
							case -1: throw new SourceException( "Missing \"", in.getLocation() );
							case '"': return new Token( TOKENTYPE.STRING, location, result.toString() );
							case '\\':
								switch( ch = in.read() )
								{
									case -1: throw new SourceException( "Incomplete escape sequence", in.getLocation() );
									case '\n': continue; // Skip newline
									case 'b': ch = '\b'; break;
									case 'f': ch = '\f'; break;
									case 'n': ch = '\n'; break;
									case 'r': ch = '\r'; break;
									case 't': ch = '\t'; break;
									case '\"': break;
									case '\\': break;
									case '$': result.append( '\\' ); break; // As is. TODO Remember, not for '' strings
									case 'u':
										char[] codePoint = new char[ 4 ];
										for( int i = 0; i < 4; i++ )
										{
											codePoint[ i ] = (char)( ch = in.read() );
											if( !( ch >= '0' && ch <= '9' ) ) // TODO What about ABCDEF?
												throw new SourceException( "Illegal escape sequence: \\u" + new String( codePoint, 0, i + 1 ), in.getLastLocation() );
										}
										ch = Integer.valueOf( new String( codePoint ), 16 );
										break;
									default:
										throw new SourceException( "Illegal escape sequence: \\" + ( ch >= 0 ? (char)ch : "" ), in.getLastLocation() );
								}
						}
						result.append( (char)ch );
					}

				// Number
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					do
					{
						result.append( (char)ch );
						ch = in.read();
					}
					while( ch >= '0' && ch <= '9' );
					boolean decimal = false;
					if( ch == '.' )
					{
						ch = in.read();
						if( !( ch >= '0' && ch <= '9' ) )
						{
							in.push( ch );
							in.push( '.' );
							return new Token( TOKENTYPE.INTEGER, location, result.toString() );
						}
						result.append( '.' );
						do
						{
							result.append( (char)ch );
							ch = in.read();
						}
						while( ch >= '0' && ch <= '9' );
						decimal = true;
					}
					if( ch == 'E' || ch == 'e' )
					{
						result.append( (char)ch );
						ch = in.read();
						if( ch == '+' || ch == '-' )
						{
							result.append( (char)ch );
							ch = in.read();
						}
						if( !( ch >= '0' && ch <= '9' ) )
							throw new SourceException( "Invalid number", in.getLocation() );
						do
						{
							result.append( (char)ch );
							ch = in.read();
						}
						while( ch >= '0' && ch <= '9' );
						decimal = true;
					}
					in.push( ch );
					return new Token( decimal ? TOKENTYPE.DECIMAL : TOKENTYPE.INTEGER, location, result.toString() );

				// Parenthesis
				case '(':
					return new Token( TOKENTYPE.PAREN_OPEN, location, "(" );
				case ')':
					return new Token( TOKENTYPE.PAREN_CLOSE, location, ")" );
				case '[':
					return new Token( TOKENTYPE.BRACKET_OPEN, location, "[" );
				case ']':
					return new Token( TOKENTYPE.BRACKET_CLOSE, location, "]" );
				case '{':
					return new Token( TOKENTYPE.BRACE_OPEN, location, "{" );
				case '}':
					return new Token( TOKENTYPE.BRACE_CLOSE, location, "}" );

				// Delimiters
				case '`':
					return new Token( TOKENTYPE.BACKQUOTE, location, "`" );
				case '\'':
					return new Token( TOKENTYPE.QUOTE, location, "'" );
				case '.':
					return new Token( TOKENTYPE.DOT, location, "." );
				case ';':
					return new Token( TOKENTYPE.SEMICOLON, location, ";" );
				case ',':
					return new Token( TOKENTYPE.COMMA, location, "," );

				// Comment
				case '/':
					int ch2 = in.read();
					if( ch2 == '/' )
					{
						do
							ch = in.read();
						while( ch != '\n' && ch != -1 );
						break;
					}
					if( ch2 == '*' )
					{
						// TODO Scala allows them to be nested
						while( true )
						{
							ch = in.read();
							if( ch == -1 )
								throw new SourceException( "Missing */", in.getLocation() );
							if( ch == '*' )
							{
								ch = in.read();
								if( ch == '/' )
									break;
								in.push( ch );
							}
						}
						break;
					}
					in.push( ch2 );

				// Operators
				// $FALL-THROUGH$
				case '!':
				case '#':
				case '%':
				case '&':
				case '*':
				case '+':
				case '-':
				case ':':
				case '<':
				case '=':
				case '>':
				case '?':
				case '@':
				case '\\':
				case '^':
				case '|':
				case '~':
					do
					{
						result.append( (char)ch );
						ch = in.read();
					}
					while( isOperatorChar( ch ) );
					in.push( ch );
					value = result.toString();
					type = RESERVED_WORDS.get( value );
					if( type != null )
						return new Token( type, location, value );
					return new Token( TOKENTYPE.OPERATOR, location, value );

				default:
					throw new SourceException( "Unexpected character '" + (char)ch + "'", in.getLocation() );
			}
		}
	}

	static private boolean isOperatorChar( int ch )
	{
		switch( ch )
		{
			case '!':
			case '#':
			case '%':
			case '&':
			case '*':
			case '+':
			case '-':
			case '/':
			case ':':
			case '<':
			case '=':
			case '>':
			case '?':
			case '@':
			case '\\':
			case '^':
			case '|':
			case '~':
				return true;
			default:
				return false;
		}
	}

	/**
	 * Close the underlying reader.
	 */
	public void close()
	{
		getIn().close();
	}

	static public class Token
	{
		private TOKENTYPE type;
		private SourceLocation location;
		private String value;

		Token( TOKENTYPE type, SourceLocation location, String value )
		{
			this.type = type;
			this.location = location;
			this.value = value;
		}

		/**
		 * @param s The string the compare this token value with.
		 *
		 * @return True if the token value is equal to the given string, false otherwise.
		 */
		public boolean eq( String s )
		{
			if( this.value == null )
				return false;
			return this.value.equals( s );
		}

		public TOKENTYPE getType()
		{
			return this.type;
		}

		public SourceLocation getLocation()
		{
			return this.location;
		}

		public String getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			if( this.type == TOKENTYPE.STRING )
				return "\"" + this.value + "\""; // TODO Or maybe just the double quote. Actually, we don't know what quote is used.
			// TODO Maybe we should not parse the complete string as a token, especially with super strings
			if( this.value != null )
				return this.value.toString();
			Assert.isTrue( this.type == TOKENTYPE.EOF );
			return "EOF";
		}
	}
}
