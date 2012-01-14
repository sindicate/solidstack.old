/*--
 * Copyright 2010 René M. de Bloois
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

package solidstack.template;

import solidstack.io.PushbackReader;


/**
 * Parses a template using the old JSP syntax.
 * 
 * @author René M. de Bloois
 */
public class JSPLikeTemplateParser
{
	static public enum EVENT { TEXT, SCRIPT, EXPRESSION, GSTRING, DIRECTIVE, COMMENT, EOF };

	static public class ParseEvent
	{
		protected EVENT event;
		protected String data;

		protected ParseEvent( EVENT event, String data )
		{
			this.event = event;
			this.data = data;
		}

		public EVENT getEvent()
		{
			return this.event;
		}

		public String getText()
		{
			return this.data;
		}
	}
	static public ParseEvent EOF = new ParseEvent( EVENT.EOF, null );

	protected PushbackReader reader;

	protected StringBuilder buffer = new StringBuilder( 1024 );


	public JSPLikeTemplateParser( PushbackReader reader )
	{
		this.reader = reader;
	}

	public ParseEvent next()
	{
		while( true )
		{
			int c = this.reader.read();
			switch( c )
			{
				case -1:
					if( this.buffer.length() > 0 )
						return new ParseEvent( EVENT.TEXT, popBuffer() );
					return EOF;

				case '\\':
					int cc = this.reader.read();
					switch( cc )
					{
						case '$':
						case '\\':
							this.buffer.append( '\\' );
							this.buffer.append( (char)cc );
							continue;
						case '<':
							this.buffer.append( (char)cc );
							continue;
						default:
							throw new ParseException( "Only <, $ or \\ can be escaped", this.reader.getLineNumber() );
					}

				case '<':
					cc = this.reader.read();
					if( cc != '%' )
					{
						this.reader.push( cc );
						this.buffer.append( '<' );
						continue;
					}
					if( this.buffer.length() > 0 )
					{
						// TODO Return text segments
						this.reader.push( cc );
						this.reader.push( c );
						return new ParseEvent( EVENT.TEXT, popBuffer() );
					}
					return readMarkup();

				case '$':
					if( this.buffer.length() > 0 )
					{
						// TODO Return text segments
						this.reader.push( c );
						return new ParseEvent( EVENT.TEXT, popBuffer() );
					}
					return readDollar();

				default:
					this.buffer.append( (char)c );
			}
		}
	}

	protected String popBuffer()
	{
		String result = this.buffer.toString();
		this.buffer.setLength( 0 );
		return result;
	}

	protected ParseEvent readMarkup()
	{
		this.reader.mark( 2 );
		int c = this.reader.read();
		switch( c )
		{
			case '=':
				ParseEvent event = readScript( this.reader );
				event.event = EVENT.EXPRESSION;
				return event;

			case '@':
				// TODO Directives should be on top only
				return readDirective( this.reader );

			case '-':
				if( this.reader.read() == '-' )
					return readComment( this.reader );
				//$FALL-THROUGH$

			default:
				this.reader.reset();
				return readScript( this.reader );
		}
	}

	protected ParseEvent readDollar()
	{
		int c = this.reader.read();
		if( c != '{' )
			throw new ParseException( "Expecting an { after the $", this.reader.getLineNumber() );
		return readGStringExpression( this.reader, true );
	}

	// Needed to parse directives
	private String getToken( PushbackReader reader )
	{
		// Skip whitespace
		int ch = reader.read();
		while( ch != -1 && Character.isWhitespace( ch ) )
		{
			if( ch == '\n' )
				this.buffer.append( (char)ch );
			ch = reader.read();
		}

		// Read a string enclosed by ' or "
		if( ch == '\'' || ch == '"' )
		{
			StringBuilder result = new StringBuilder( 32 );
			int quote = ch;
			while( true )
			{
				result.append( (char)ch );

				ch = reader.read();
				if( ch == -1 || ch == '\n' )
					throw new ParseException( "Unclosed string", reader.getLineNumber() );
				if( ch == quote )
				{
					result.append( (char)ch );
					return result.toString();
				}
			}
		}

		// Read an identifier
		if( Character.isJavaIdentifierStart( ch ) && ch != '$' )
		{
			StringBuilder result = new StringBuilder( 16 );
			while( true )
			{
				result.append( (char)ch );
				ch = reader.read();
				if( !Character.isJavaIdentifierPart( ch ) || ch == '$' )
				{
					reader.push( ch );
					return result.toString();
				}
			}
		}

		// Read %>
		if( ch == '%' )
		{
			ch = reader.read();
			if( ch != '>' )
				throw new ParseException( "Expecting > after an %", reader.getLineNumber() );
			return "%>";
		}

		if( ch == -1 )
			return null;

		return String.valueOf( (char)ch );
	}

	protected ParseEvent readDirective( PushbackReader reader )
	{
		String name = getToken( reader );
		if( name == null )
			throw new ParseException( "Expecting a name", reader.getLineNumber() );

		String token = getToken( reader );
		while( token != null )
		{
			if( token.equals( "%>" ) )
				return new ParseEvent( EVENT.DIRECTIVE, popBuffer() ); // TODO directives
			if( !getToken( reader ).equals( "=" ) )
				throw new ParseException( "Expecting '=' in directive", reader.getLineNumber() );
			String value = getToken( reader );
			if( value == null || !value.startsWith( "\"" ) || !value.endsWith( "\"" ) )
				throw new ParseException( "Expecting a string value in directive", reader.getLineNumber() );
//			writer.directive( name, token, value.substring( 1, value.length() - 1 ), reader.getLineNumber() );
			token = getToken( reader );
		}

		throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
	}

	private StringBuilder readWhitespace( PushbackReader reader )
	{
		StringBuilder builder = new StringBuilder();
		int c = reader.read();
		while( Character.isWhitespace( (char)c ) && c != '\n' )
		{
			builder.append( (char)c );
			c = reader.read();
		}
		reader.push( c );
		return builder;
	}

	private ParseEvent readScript( PushbackReader reader )
	{
		// We are in SCRIPT/EXPRESSION mode here.
		// Expecting ", ' or %>
		// %> within strings should not end the script

		while( true )
		{
			int c = reader.read();
			switch( c )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );

				case '"':
				case '\'':
					readString( reader, (char)c );
					break;

				case '%':
					int cc = reader.read();
					if( cc == '>' )
						return new ParseEvent( EVENT.SCRIPT, popBuffer() );
					reader.push( cc );
					//$FALL-THROUGH$

				default:
					this.buffer.append( (char)c );
			}
		}
	}

	private void readString( PushbackReader reader, char quote )
	{
		// String can be read in any mode
		// Expecting $, ", ' and \
		// " within ${} should not end this string
		// \ is used to escape $, " and itself, and special characters

		this.buffer.append( quote );
		boolean multiline = false;
		reader.mark( 2 );
		if( reader.read() == quote && reader.read() == quote )
		{
			multiline = true;
			this.buffer.append( quote );
			this.buffer.append( quote );
		}
		else
			reader.reset();

		while( true )
		{
			int c = reader.read();

			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( !multiline && c == '\n' )
				throw new ParseException( "Unexpected end of line", reader.getLineNumber() );

			if( c == '\\' )
			{
				this.buffer.append( (char)c );
				c = reader.read();
				if( "bfnrt'\"\\$".indexOf( c ) >= 0 )
					this.buffer.append( (char)c );
				else
					throw new ParseException( "Only b, f, n, r, t, ', \",  $ or \\ can be escaped", reader.getLineNumber() );
				continue;
			}

			if( quote == '"' && c == '$' )
			{
				c = reader.read();
				if( c != '{' )
					throw new ParseException( "Expecting an { after the $", reader.getLineNumber() );
				this.buffer.append( '$' );
				this.buffer.append( '{' );
				readGStringExpression( reader, multiline );
				this.buffer.append( '{' );
				continue;
			}

			if( c == quote )
			{
				this.buffer.append( quote );
				if( !multiline )
					return;

				reader.mark( 2 );
				if( reader.read() == quote && reader.read() == quote )
				{
					this.buffer.append( quote );
					this.buffer.append( quote );
					break;
				}

				reader.reset();
				continue;
			}

			this.buffer.append( (char)c );
		}
	}

	protected ParseEvent readGStringExpression( PushbackReader reader, boolean multiline )
	{
		// GStringExpressions can be read in any mode
		// Expecting }, ", ' and {
		// } within a string should not end this expression

		while( true )
		{
			int c = reader.read();
			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( !multiline && c == '\n' )
				throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
			if( c == '}' )
				return new ParseEvent( EVENT.GSTRING, popBuffer() );
			if( c == '"' || c == '\'' )
				readString( reader, (char)c );
			else if( c =='{' )
				readBlock( reader, multiline );
			else
				this.buffer.append( (char)c );
		}
	}

	private void readBlock( PushbackReader reader, boolean multiline )
	{
		// Expecting }, " or '
		// } within a string should not end this block

		this.buffer.append( '{' );
		while( true )
		{
			int c = reader.read();
			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( !multiline && c == '\n' )
				throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
			if( c == '}' )
				break;
			if( c == '"' || c == '\'' )
				readString( reader, (char)c );
			else if( c =='{' )
				readBlock( reader, multiline );
			else
				this.buffer.append( (char)c );
		}
		this.buffer.append( '}' );
	}

	protected ParseEvent readComment( PushbackReader reader )
	{
		// We are in SCRIPT mode (needed to transfer newlines from the comment to the script)
		// Expecting --%>, <%--, \n
		// Comments can be nested
		// Newlines are transferred to the script

		while( true )
		{
			int c = reader.read();
			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( c == '-' )
			{
				reader.mark( 10 );
				if( reader.read() == '-' && reader.read() == '%' && reader.read() == '>' )
					return new ParseEvent( EVENT.COMMENT, popBuffer() );
				reader.reset();
			}
			else if( c == '<' )
			{
				reader.mark( 10 );
				if( reader.read() == '%' && reader.read() == '-' && reader.read() == '-' )
					readComment( reader );
				else
					reader.reset();
			}
			else if( c == '\n' )
			{
				this.buffer.append( '\n' );
			}
		}
	}
}
