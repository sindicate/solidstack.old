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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solidbase.io.PushbackReader;
import solidstack.Assert;


/**
 * Parses a template using the old JSP syntax.
 * 
 * @author René M. de Bloois
 */
public class JSPLikeTemplateParser
{
	static public enum EVENT { TEXT, WHITESPACE, NEWLINE, SCRIPT, EXPRESSION, GSTRING, DIRECTIVE, COMMENT, EOF };

	static public ParseEvent EOF = new ParseEvent( EVENT.EOF, null );
	static public ParseEvent NEWLINE = new ParseEvent( EVENT.NEWLINE, "\n" );

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
						// TODO Return text segments, blocks should not be longer than a maximum
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

	public ParseEvent next2()
	{
		// This loop always runs in text mode, no scripts or anything else

		boolean textFound = false;
		while( true )
		{
			int c = this.reader.read();
			switch( c )
			{
				case -1:
					if( this.buffer.length() > 0 )
						return new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
					return EOF;

				case '\\':
					int cc = this.reader.read();
					textFound = true;
					switch( cc )
					{
						case '$':
						case '\\':
							this.buffer.append( '\\' ); // TODO This may not be correct, re-escaping should be done somewhere else
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
						textFound = true;
						continue;
					}
					if( this.buffer.length() > 0 )
					{
						// TODO Return text segments, blocks should not be longer than a maximum
						this.reader.push( cc );
						this.reader.push( c );
						ParseEvent result = new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
						textFound = false;
						return result;
					}
					return readMarkup();

				case '$':
					if( this.buffer.length() > 0 )
					{
						// TODO Return text segments
						this.reader.push( c );
						ParseEvent result = new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
						textFound = false;
						return result;
					}
					return readDollar();

				case '\n':
					if( this.buffer.length() > 0 )
					{
						this.reader.push( c );
						ParseEvent result = new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
						textFound = false;
						return result;
					}
					return NEWLINE;

				default:
					textFound = true;
					//$FALL-THROUGH$

				case '\t':
				case ' ':
					this.buffer.append( (char)c );
			}
		}
	}

	protected List< ParseEvent > queue = new ArrayList< ParseEvent >();

	public ParseEvent next3()
	{
		if( this.queue.size() > 0 )
			return this.queue.remove( 0 );

		while( true )
		{
			ParseEvent event = next2();
			switch( event.getEvent() )
			{
				case TEXT:
				case EXPRESSION:
				case GSTRING:
				case EOF:
					if( this.queue.size() == 0 )
						return event;
					this.queue.add( event );
					return this.queue.remove( 0 );

				case WHITESPACE:
				case SCRIPT:
				case DIRECTIVE:
				case COMMENT:
					this.queue.add( event );
					break;

				case NEWLINE:
					if( this.queue.size() == 0 )
						return event;
					this.queue.add( event );
					processQueue();
					return this.queue.remove( 0 );
			}
		}
	}

	protected void processQueue()
	{
		int index = 0;
		while( index < this.queue.size() )
		{
			ParseEvent event = this.queue.get( index );
			index++;
			switch( event.getEvent() )
			{
				case WHITESPACE:
				case NEWLINE:
					if( index < this.queue.size() )
					{
						ParseEvent event2 = this.queue.get( index );
						switch( event2.getEvent() )
						{
							case SCRIPT:
							case DIRECTIVE:
								event2.setData( event.getData() + event2.getData() );
								//$FALL-THROUGH$
							case COMMENT:
								index--;
								this.queue.remove( index );
								//$FALL-THROUGH$
							default:
						}
					}
					else
					{
						index -= 2;
						if( index >= 0 )
						{
							ParseEvent event2 = this.queue.get( index );
							switch( event2.getEvent() )
							{
								case SCRIPT:
								case DIRECTIVE:
									event2.setData( event2.getData() + event.getData() );
									index++;
									this.queue.remove( index );
									break;
								case COMMENT:
									if( event.getEvent() == EVENT.NEWLINE )
										event2.setData( event2.getData() + event.getData() );
									index++;
									this.queue.remove( index );
									//$FALL-THROUGH$
								default:
							}
						}
					}
					break;
				case DIRECTIVE:
				case SCRIPT:
				case COMMENT:
					break;
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
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
		readGStringExpression( this.reader, true );
		return new ParseEvent( EVENT.GSTRING, popBuffer() );
	}

	// Needed to parse directives
	private String getToken( PushbackReader reader )
	{
		// Skip whitespace
		int ch = reader.read();
		while( ch != -1 && Character.isWhitespace( ch ) ) // TODO Bad whitespace
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

		ParseEvent result = new ParseEvent( EVENT.DIRECTIVE );

		String token = getToken( reader );
		while( token != null )
		{
			if( token.equals( "%>" ) )
			{
				result.setData( popBuffer() );
				return result;
			}
			if( !getToken( reader ).equals( "=" ) )
				throw new ParseException( "Expecting '=' in directive", reader.getLineNumber() );
			String value = getToken( reader );
			if( value == null || !value.startsWith( "\"" ) || !value.endsWith( "\"" ) )
				throw new ParseException( "Expecting a string value in directive", reader.getLineNumber() );
			result.addDirective( name, token, value.substring( 1, value.length() - 1 ), reader.getLineNumber() );
			token = getToken( reader );
		}

		throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
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
				this.buffer.append( '}' );
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

	protected void readGStringExpression( PushbackReader reader, boolean multiline )
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
				return;
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

	static public class ParseEvent
	{
		private EVENT event;
		private String data;
		private List< Directive > directives;

		public ParseEvent( EVENT event )
		{
			this( event, null );
		}

		public ParseEvent( EVENT event, String data )
		{
			this.event = event;
			this.data = data;
		}

		public EVENT getEvent()
		{
			return this.event;
		}

		public String getData()
		{
			return this.data;
		}

		public void setData( String data )
		{
			this.data = data;
		}

		@Override
		public String toString()
		{
			return this.event + ": " + this.data;
		}

		public void addDirective( String name, String attribute, String value, int lineNumber )
		{
			if( this.directives == null )
				this.directives = new ArrayList< Directive >();
			this.directives.add( new Directive( name, attribute, value, lineNumber ) );
		}

		public List< Directive > getDirectives()
		{
			if( this.directives == null )
				return Collections.emptyList();
			return this.directives;
		}
	}

	static public class Directive
	{
		private String name;
		private String attribute;
		private String value;
		private int lineNumber;

		protected Directive( String name, String attribute, String value, int lineNumber )
		{
			this.name = name;
			this.attribute = attribute;
			this.value = value;
			this.lineNumber = lineNumber;
		}

		public String getAttribute()
		{
			return this.attribute;
		}

		public String getValue()
		{
			return this.value;
		}

		public String getName()
		{
			return this.name;
		}
	}
}
