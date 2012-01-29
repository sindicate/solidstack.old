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
import java.util.Iterator;
import java.util.List;
import solidbase.io.LineReader;
import solidbase.io.PushbackReader;
import solidstack.Assert;


/**
 * A pull parser for the JSP template syntax.
 * 
 * @author René M. de Bloois
 */
public class JSPLikeTemplateParser
{
	/**
	 * Possible pull events.
	 */
	@SuppressWarnings( "hiding" )
	static public enum EVENT
	{
		/**
		 * A text event, no newlines.
		 */
		TEXT,
		/**
		 * A text event but only whitespace, no newlines.
		 */
		WHITESPACE,
		/**
		 * A newline.
		 */
		NEWLINE,
		/**
		 * A <% script.
		 */
		SCRIPT,
		/**
		 * A <%= expression.
		 */
		EXPRESSION,
		/**
		 * A $ or ${ expression.
		 */
		EXPRESSION2,
		/**
		 * A <%@ directive.
		 */
		DIRECTIVE,
		/**
		 * A comment.
		 */
		COMMENT,
		/**
		 * The end of file.
		 */
		EOF
	}

	/**
	 * The EOF parse event.
	 */
	static public ParseEvent EOF = new ParseEvent( EVENT.EOF, null );

	/**
	 * The newline parse event.
	 */
	static public ParseEvent NEWLINE = new ParseEvent( EVENT.NEWLINE, "\n" );

	/**
	 * The reader from which the source of the template is read.
	 */
	private PushbackReader reader;

	/**
	 * A buffer that is used to build up text and whitespace events.
	 */
	private StringBuilder buffer = new StringBuilder( 1024 );

	/**
	 * This queue of parse events is used to consolidate whitespace. This means that when scripts, comments and
	 * directives are completely contained in their own lines in the template, the surrounding whitespace is assigned to
	 * the script, comment and directive events and no whitespace events are triggered.
	 */
	private List< ParseEvent > queue = new ArrayList< ParseEvent >();

	/**
	 * Constructor.
	 * 
	 * @param reader The reader from which to read the source of the template.
	 */
	public JSPLikeTemplateParser( LineReader reader )
	{
		this.reader = new PushbackReader( reader );
	}

	/**
	 * Retrieves the next event.
	 * 
	 * @return The next event.
	 */
	public ParseEvent next()
	{
		if( this.queue.size() > 0 )
			return this.queue.remove( 0 );

		ParseEvent event;
		while( true )
			switch( ( event = next0() ).getEvent() )
			{
				case TEXT:
				case EXPRESSION:
				case EXPRESSION2:
				case EOF:
					if( this.queue.size() == 0 )
						return event; // Just pass through
					this.queue.add( event );
					return this.queue.remove( 0 ); // The queue can now be emptied again

				case WHITESPACE:
				case SCRIPT:
				case DIRECTIVE:
				case COMMENT:
					this.queue.add( event ); // Need to wait for the rest
					break;

				case NEWLINE:
					if( this.queue.size() == 0 )
						return event; // Just pass through
					this.queue.add( event );
					reassignNewlines(); // We need to reassign the whitespace because no template text has been found on the last lines
					return this.queue.remove( 0 ); // The queue can now be emptied again
			}
	}

	/**
	 * Retrieves the next event, but does not consolidate whitespace when scripts, comments or directives are completely contained in separate lines in the template.
	 * 
	 * @return The next event.
	 */
	private ParseEvent next0()
	{
		PushbackReader reader = this.reader;
		StringBuilder buffer = this.buffer;
		int c;
		boolean textFound = false;

		while( true )
			switch( c = reader.read() )
			{
				case -1:
					if( buffer.length() > 0 )
						return new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
					return EOF;

				case '\\':
					textFound = true;
					switch( c = reader.read() )
					{
						case '$':
						case '\\':
						case '<':
							buffer.append( (char)c );
							continue;
						default:
							throw new ParseException( "Only <, $ or \\ can be escaped", reader.getLineNumber() );
					}

				case '<':
					c = reader.read();
					if( c != '%' )
					{
						reader.push( c );
						buffer.append( '<' );
						textFound = true;
						continue;
					}
					if( buffer.length() > 0 )
					{
						// TODO Return text segments with maximum size, so that we can parse huge files that have no newlines
						reader.push( c );
						reader.push( '<' );
						ParseEvent result = new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
						textFound = false;
						return result;
					}
					return readMarkup();

				case '$':
					if( buffer.length() > 0 )
					{
						reader.push( c );
						ParseEvent result = new ParseEvent( textFound ? EVENT.TEXT : EVENT.WHITESPACE, popBuffer() );
						textFound = false;
						return result;
					}
					return readDollar();

				case '\n':
					if( buffer.length() > 0 )
					{
						reader.push( c );
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
					buffer.append( (char)c );
			}
	}

	/**
	 * Consolidates whitespace and newlines.
	 */
	private void reassignNewlines()
	{
		// Remove all whitespace
		for( Iterator< ParseEvent > i = this.queue.iterator(); i.hasNext(); )
			if( i.next().getEvent() == EVENT.WHITESPACE )
				i.remove();

		// And reassign newlines
		int index = 0;
		while( index < this.queue.size() )
		{
			ParseEvent event = this.queue.get( index++ );
			ParseEvent event2;
			switch( event.getEvent() )
			{
				case NEWLINE:
					if( index >= this.queue.size() ) // Is it the last one?
					{
						index -= 2;
						Assert.isTrue( index >= 0 );
						switch( ( event2 = this.queue.get( index ) ).getEvent() ) // TODO This whole switch is only for the assertion failure
						{
							case SCRIPT:
							case DIRECTIVE:
							case COMMENT:
								event2.setData( event2.getData() + event.getData() );
								this.queue.remove( ++index );
								return;
							default:
								Assert.fail( "Should not come here" );
						}
					}
					switch( ( event2 = this.queue.get( index ) ).getEvent() )
					{
						case SCRIPT:
						case DIRECTIVE:
						case COMMENT:
						case NEWLINE:
							event2.setData( event.getData() + event2.getData() );
							this.queue.remove( --index );
							break;
						default:
							Assert.fail( "Should not come here" );
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

	/**
	 * Returns the contents of the buffer, and clears it.
	 * 
	 * @return The contents of the buffer.
	 */
	private String popBuffer()
	{
		String result = this.buffer.toString();
		this.buffer.setLength( 0 );
		return result;
	}

	/**
	 * Reads <% markup. Could be a script, expression, directive or comment.
	 * 
	 * @return The markup.
	 */
	private ParseEvent readMarkup()
	{
		this.reader.mark( 2 );
		int c = this.reader.read();
		switch( c )
		{
			case '=':
				return readScript( EVENT.EXPRESSION );
			case '@':
				return readDirective();
			case '-':
				if( this.reader.read() == '-' )
					return readComment();
				//$FALL-THROUGH$
			default:
				this.reader.reset();
				return readScript( EVENT.SCRIPT );
		}
	}

	/**
	 * Reads a ${ expression.
	 * 
	 * @return The ${ expression.
	 */
	// TODO Can we test if closures also in $ expressions? And with the <%= expressions?
	// TODO We should understand $var too? Like in Groovy?
	private ParseEvent readDollar()
	{
		int c = this.reader.read();
		if( c != '{' )
			throw new ParseException( "Expecting an { after the $", this.reader.getLineNumber() );
		readGStringExpression( true );
		return new ParseEvent( EVENT.EXPRESSION2, popBuffer() );
	}

	/**
	 * Reads a token.
	 * 
	 * @return A token.
	 */
	private String getToken()
	{
		PushbackReader reader = this.reader;

		// Skip whitespace
		int ch;
		loop: while( true )
		{
			switch( ch = reader.read() )
			{
				default:
					break loop;
				case '\n':
					this.buffer.append( '\n' ); //$FALL-THROUGH$
				case ' ':
				case '\t':
				case '\r':
			}
		}

		switch( ch )
		{
			case -1:
				return null;
			case '\'':
			case '"':
				// Read a string enclosed by ' or "
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
			case '%':
				// Read %>
				ch = reader.read();
				if( ch != '>' )
					throw new ParseException( "Expecting > after an %", reader.getLineNumber() );
				return "%>";
			default:
				// Read an identifier
				if( Character.isJavaIdentifierStart( ch ) ) // TODO Don't use java identifier start?
				{
					result = new StringBuilder( 16 );
					while( true )
					{
						result.append( (char)ch );
						ch = reader.read();
						if( !Character.isJavaIdentifierPart( ch ) || ch == '$' ) // TODO Why is $ special?
						{
							reader.push( ch );
							return result.toString();
						}
					}
				}
				//$FALL-THROUGH$
			case '$': // TODO Why is $ special?
				return String.valueOf( (char)ch );
		}
	}

	/**
	 * Reads a directive.
	 * 
	 * @return A directive.
	 */
	private ParseEvent readDirective()
	{
		PushbackReader reader = this.reader;

		String name = getToken();
		if( name == null )
			throw new ParseException( "Expecting a name", reader.getLineNumber() );

		ParseEvent result = new ParseEvent( EVENT.DIRECTIVE );

		String token = getToken();
		while( token != null )
		{
			if( token.equals( "%>" ) )
			{
				result.setData( popBuffer() );
				return result;
			}
			if( !getToken().equals( "=" ) )
				throw new ParseException( "Expecting '=' in directive", reader.getLineNumber() );
			String value = getToken();
			if( value == null || !value.startsWith( "\"" ) || !value.endsWith( "\"" ) )
				throw new ParseException( "Expecting a string value in directive", reader.getLineNumber() );
			result.addDirective( name, token, value.substring( 1, value.length() - 1 ), reader.getLineNumber() );
			token = getToken();
		}

		throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
	}

	private ParseEvent readScript( EVENT event )
	{
		// We are in SCRIPT/EXPRESSION mode here.
		// Expecting ", ' or %>
		// %> within strings should not end the script

		PushbackReader reader = this.reader;
		StringBuilder buffer = this.buffer;

		while( true )
		{
			int c = reader.read();
			switch( c )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );

				case '"':
				case '\'':
					readString( (char)c );
					break;

				case '%':
					int cc = reader.read();
					if( cc == '>' )
						return new ParseEvent( event, popBuffer() );
					reader.push( cc );
					//$FALL-THROUGH$

				default:
					buffer.append( (char)c );
			}
		}
	}

	private void readString( char quote )
	{
		// String can be read in any mode
		// Expecting $, ", ' and \
		// " within ${} should not end this string
		// \ is used to escape $, " and itself, and special characters

		PushbackReader reader = this.reader;
		StringBuilder buffer = this.buffer;

		buffer.append( quote );
		boolean multiline = false;
		reader.mark( 2 );
		if( reader.read() == quote && reader.read() == quote )
		{
			multiline = true;
			buffer.append( quote );
			buffer.append( quote );
		}
		else
			reader.reset();

		int c;
		while( true )
			switch( c = reader.read() )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
				case '\n':
					if( !multiline )
						throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
					buffer.append( (char)c );
					break;
				case '\\':
					buffer.append( (char)c );
					switch( c = reader.read() )
					{
						default:
							throw new ParseException( "Only b, f, n, r, t, ', \",  $ or \\ can be escaped", reader.getLineNumber() );
						case 'b':
						case 'f':
						case 'n':
						case 'r':
						case 't':
						case '\'':
						case '"':
						case '\\':
						case '$':
							buffer.append( (char)c );
					}
					break;
				case '$':
					if( quote == '"' )
					{
						c = reader.read();
						if( c != '{' )
							throw new ParseException( "Expecting an { after the $", reader.getLineNumber() );
						buffer.append( '$' );
						buffer.append( '{' );
						readGStringExpression( multiline );
						buffer.append( '}' );
						break;
					}
					buffer.append( (char)c );
					break;
				case '"':
				case '\'':
					if( c == quote )
					{
						buffer.append( quote );
						if( !multiline )
							return;

						reader.mark( 2 );
						if( reader.read() == quote && reader.read() == quote )
						{
							buffer.append( quote );
							buffer.append( quote );
							return;
						}

						reader.reset();
						break;
					}
					buffer.append( (char)c );
					break;
				default:
					buffer.append( (char)c );
			}
	}

	private void readGStringExpression( boolean multiline )
	{
		// GStringExpressions can be read in any mode
		// Expecting }, ", ' and {
		// } within a string should not end this expression

		PushbackReader reader = this.reader;
		StringBuilder buffer = this.buffer;

		int c;
		while( true )
			switch( c = reader.read() )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
				case '}':
					return;
				case '"':
				case '\'':
					readString( (char)c );
					break;
				case '{':
					readBlock( multiline );
					break;
				case '\n':
					if( !multiline  )
						throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
					//$FALL-THROUGH$
				default:
					buffer.append( (char)c );
			}
	}

	private void readBlock( boolean multiline )
	{
		// Expecting }, " or '
		// } within a string should not end this block

		PushbackReader reader = this.reader;
		StringBuilder buffer = this.buffer;

		buffer.append( '{' );

		int c;
		while( true )
			switch( c = reader.read() )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
				case '}':
					buffer.append( '}' );
					return;
				case '"':
				case '\'':
					readString( (char)c );
					break;
				case '{':
					readBlock( multiline );
					break;
				case '\n':
					if( !multiline  )
						throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
					//$FALL-THROUGH$
				default:
					buffer.append( (char)c );
			}
	}

	private ParseEvent readComment()
	{
		// We are in SCRIPT mode (needed to transfer newlines from the comment to the script)
		// Expecting --%>, <%--, \n
		// Comments can be nested
		// Newlines are transferred to the script

		PushbackReader reader = this.reader;

		while( true )
			switch( reader.read() )
			{
				case -1:
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
				case '-':
					reader.mark( 3 );
					if( reader.read() == '-' && reader.read() == '%' && reader.read() == '>' )
						return new ParseEvent( EVENT.COMMENT, popBuffer() );
					reader.reset();
					break;
				case '<':
					reader.mark( 3 );
					if( reader.read() == '%' && reader.read() == '-' && reader.read() == '-' )
					{
						readComment();
						break;
					}
					reader.reset();
					break;
				case '\n':
					this.buffer.append( '\n' ); // Only newlines are kept
			}
	}

	/**
	 * Represents a parse event.
	 */
	static public class ParseEvent
	{
		private EVENT event;
		private String data;
		private List< Directive > directives;

		/**
		 * Constructor.
		 * 
		 * @param event The type of the event.
		 */
		public ParseEvent( EVENT event )
		{
			this( event, null );
		}

		/**
		 * Constructor.
		 * 
		 * @param event The type of the event.
		 * @param data The string data belonging to the event.
		 */
		public ParseEvent( EVENT event, String data )
		{
			this.event = event;
			this.data = data;
		}

		/**
		 * Returns the type of the event.
		 * 
		 * @return The type of the event.
		 */
		public EVENT getEvent()
		{
			return this.event;
		}

		/**
		 * Returns the string data belonging to the event.
		 * 
		 * @return The string data belonging to the event.
		 */
		public String getData()
		{
			return this.data;
		}

		/**
		 * Returns the directives belonging to this event.
		 * 
		 * @return The directives belonging to this event.
		 */
		public List< Directive > getDirectives()
		{
			if( this.directives == null )
				return Collections.emptyList();
			return this.directives;
		}

		/**
		 * Sets the string data belonging to the event.
		 * 
		 * @param data The string data.
		 */
		void setData( String data )
		{
			this.data = data;
		}

		void addDirective( String name, String attribute, String value, int lineNumber )
		{
			if( this.directives == null )
				this.directives = new ArrayList< Directive >();
			this.directives.add( new Directive( name, attribute, value, lineNumber ) );
		}

		@Override
		public String toString()
		{
			return this.event + ": " + this.data;
		}
	}

	/**
	 * A directive found in the template.
	 */
	static public class Directive
	{
		private String name;
		private String attribute;
		private String value;
		private int lineNumber;

		Directive( String name, String attribute, String value, int lineNumber )
		{
			this.name = name;
			this.attribute = attribute;
			this.value = value;
			this.lineNumber = lineNumber;
		}

		/**
		 * Returns the name of the directive.
		 * 
		 * @return The name of the directive.
		 */
		public String getName()
		{
			return this.name;
		}

		/**
		 * Returns the attribute name.
		 * 
		 * @return The attribute name.
		 */
		public String getAttribute()
		{
			return this.attribute;
		}

		/**
		 * Returns the value of the attribute.
		 * 
		 * @return The value of the attribute.
		 */
		public String getValue()
		{
			return this.value;
		}

		/**
		 * Returns the line number of the directive in the source file.
		 * 
		 * @return The line number of the directive in the source file.
		 */
		public int getLineNumber()
		{
			return this.lineNumber;
		}
	}
}
