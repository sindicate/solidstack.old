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

import solidstack.Assert;
import solidstack.io.PushbackReader;
import solidstack.template.JSPLikeTemplateParser.ModalWriter.Mode;


/**
 * Parses a template using the old JSP syntax.
 * 
 * @author René M. de Bloois
 */
public class JSPLikeTemplateParser
{
	/**
	 * Parse the template.
	 * 
	 * @param reader The input reader.
	 * @param writer This writer produces the right Groovy code.
	 * @return The resulting Groovy code.
	 */
	public String parse( PushbackReader reader, ModalWriter writer )
	{
		StringBuilder leading = readWhitespace( reader );
		while( true )
		{
			writer.nextMode( Mode.TEXT );

			// We are in TEXT mode here.
			// Expecting <%, <%=, <%--, ${
			// " must be escaped because text is appended with a append("""...""")
			// \n must be detected because we want to read leading whitespace in a special way
			// Only <, $ and \ may be escaped

			int c = reader.read();
			if( c == -1 )
				break;

			if( c == '\\' )
			{
				writer.write( leading ); leading = null;
				int cc = reader.read();
				if( cc == '$' || cc == '\\' )
				{
					writer.write( (char)c );
					writer.write( (char)cc );
				}
				else if( cc == '<' )
					writer.write( (char)cc );
				else
					throw new ParseException( "Only <, $ or \\ can be escaped", reader.getLineNumber() );
				continue;
			}

			if( c == '<' )
			{
				int cc = reader.read();
				if( cc != '%' )
				{
					writer.write( leading ); leading = null;
					writer.write( (char)c );
					reader.push( cc );
					continue;
				}

				reader.mark( 2 );
				c = reader.read();

				if( c == '=' )
				{
					writer.write( leading ); leading = null;
					writer.nextMode( Mode.EXPRESSION );
					readScript( reader, writer );
					continue;
				}

				if( c == '@' )
				{
					if( leading == null )
					{
						readDirective( reader, writer );
					}
					else
					{
						// Directive started with leading whitespace only
						readDirective( reader, writer );
						StringBuilder trailing = readWhitespace( reader );

						c = reader.read();
						if( (char)c == '\n' )
						{
							// Directive on its own lines, leading and trailing whitespace are ignored
							writer.nextMode( Mode.SCRIPT );
							writer.write( '\n' ); // Must not lose newlines
							leading = readWhitespace( reader );
						}
						else
						{
							reader.push( c );
							writer.nextMode( Mode.TEXT );
							writer.write( leading ); leading = null;
							writer.write( trailing );
						}
					}
					continue;
				}

				if( c == '-' && reader.read() == '-' )
				{
					writer.nextMode( Mode.SCRIPT );
					if( leading == null )
						readComment( reader, writer );
					else
					{
						// Comment started with leading whitespace only
						readComment( reader, writer );
						StringBuilder trailing = readWhitespace( reader );
						c = reader.read();
						if( (char)c == '\n' )
						{
							writer.write( '\n' ); // Must not lose newlines
							leading = readWhitespace( reader ); // Comment on its own lines, ignore the lines totally
						}
						else
						{
							reader.push( c );
							writer.nextMode( Mode.TEXT );
							writer.write( leading ); leading = null;
							writer.write( trailing );
						}
					}
					continue;
				}

				reader.reset();

				if( leading == null )
				{
					writer.nextMode( Mode.SCRIPT );
					readScript( reader, writer );
				}
				else
				{
					// ASSUMPTION: SCRIPT has no adornments

					// Script started with leading whitespace only, transform the script into a new buffer
					ModalWriter buffer = new ScriptWriter();
					readScript( reader, buffer );
					StringBuilder trailing = readWhitespace( reader );

					c = reader.read();
					if( (char)c == '\n' )
					{
						// Script on its own lines, leading and trailing whitespace are added to the script instead of the text
						writer.nextMode( Mode.SCRIPT );
						writer.write( leading ); leading = null;
						writer.write( buffer.getBuffer() );
						writer.write( trailing );
						writer.write( '\n' ); // Must not lose newlines
						leading = readWhitespace( reader );
					}
					else
					{
						reader.push( c );
						writer.nextMode( Mode.TEXT );
						writer.write( leading ); leading = null;
						writer.nextMode( Mode.SCRIPT );
						writer.write( buffer.getBuffer() );
						writer.nextMode( Mode.TEXT );
						writer.write( trailing );
					}
				}

				continue;
			}

			writer.write( leading ); leading = null;

			if( c == '$' )
			{
				int cc = reader.read();
				if( cc != '{' )
					throw new ParseException( "Expecting an { after the $", reader.getLineNumber() );
				readGStringExpression( reader, writer, true );
				continue;
			}

			if( c == '"' )
			{
				// Because we are in a """ string, we need to add escaping to a "
				writer.write( '\\' );
				writer.write( (char)c );
				continue;
			}

			if( c == '\n' )
			{
				// Newline, we need to read leading whitespace
				writer.write( (char)c );
				leading = readWhitespace( reader );
				continue;
			}

			writer.write( (char)c );
		}

		writer.nextMode( Mode.TEXT );
		writer.write( leading );
		writer.activateMode( Mode.SCRIPT ); // ASSUMPTION: Script is not decorated

		return writer.getResult();
	}

	static private String getToken( PushbackReader reader )
	{
		// Skip whitespace
		int ch = reader.read();
		while( ch != -1 && Character.isWhitespace( ch ) )
			ch = reader.read();

		// Read a string enclosed by ' or "
		if( ch == '\'' || ch == '"' )
		{
			StringBuilder result = new StringBuilder( 32 );
			int quote = ch;
			while( true )
			{
				result.append( (char)ch );

				ch = reader.read();
				if( ch == -1 )
					throw new ParseException( "Unexpected end of input", reader.getLineNumber() );
				if( ch == quote )
				{
					result.append( (char)ch );
					break;
				}
			}
			return result.toString();
		}

		if( ch == '%' )
		{
			ch = reader.read();
			if( ch == -1 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( ch == '>' )
				return "%>";
			reader.push( ch );
			return "%";
		}

		if( ch == '=' )
			return String.valueOf( (char)ch );

		if( ch == -1 )
			return null;

		// Collect all characters until whitespace or special character
		StringBuilder result = new StringBuilder( 16 );
		do
		{
			result.append( (char)ch );
			ch = reader.read();
		}
		while( ch != -1 && !Character.isWhitespace( ch ) && ch != '=' && ch != '%' );

		// Push back the last character
		reader.push( ch );

		// Return the result
		Assert.isFalse( result.length() == 0 );
		return result.toString();
	}

	private void readDirective( PushbackReader reader, ModalWriter writer )
	{
		String name = getToken( reader );
		if( name == null )
			throw new ParseException( "Expecting a name", reader.getLineNumber() );

		String token = getToken( reader );
		while( token != null )
		{
			if( token.equals( "%>" ) )
				return;
			if( !getToken( reader ).equals( "=" ) )
				throw new ParseException( "Expecting '=' in directive", reader.getLineNumber() );
			String value = getToken( reader );
			if( value == null || !value.startsWith( "\"" ) || !value.endsWith( "\"" ) )
				throw new ParseException( "Expecting a string value in directive", reader.getLineNumber() );
			writer.directive( name, token, value.substring( 1, value.length() - 1 ), reader.getLineNumber() );
			token = getToken( reader );
		}
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

	private void readScript( PushbackReader reader, ModalWriter writer )
	{
		Assert.isTrue( writer.nextMode == Mode.SCRIPT || writer.nextMode == Mode.EXPRESSION );

		// We are in SCRIPT/EXPRESSION mode here.
		// Expecting ", ' or %>
		// %> within strings should not end the script

		while( true )
		{
			int c = reader.read();
			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( c == '"' || c == '\'' )
				readString( reader, writer, (char)c );
			else if( c == '%' )
			{
				c = reader.read();
				if( c == '>' )
					break;
				reader.push( c );
				writer.write( '%' );
			}
			else
				writer.write( (char)c );
		}
	}

	private void readString( PushbackReader reader, ModalWriter writer, char quote )
	{
		Assert.isTrue( writer.nextMode == Mode.EXPRESSION || writer.nextMode == Mode.SCRIPT || writer.nextMode == Mode.TEXT, "Unexpected mode " + writer.nextMode );

		// String can be read in any mode
		// Expecting $, ", ' and \
		// " within ${} should not end this string
		// \ is used to escape $, " and itself, and special characters

		writer.write( quote );
		boolean multiline = false;
		reader.mark( 2 );
		if( reader.read() == quote && reader.read() == quote )
		{
			multiline = true;
			writer.write( quote );
			writer.write( quote );
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
				writer.write( (char)c );
				c = reader.read();
				if( "bfnrt'\"\\$".indexOf( c ) >= 0 )
					writer.write( (char)c );
				else
					throw new ParseException( "Only b, f, n, r, t, ', \",  $ or \\ can be escaped", reader.getLineNumber() );
				continue;
			}

			if( quote == '"' && c == '$' )
			{
				c = reader.read();
				if( c != '{' )
					throw new ParseException( "Expecting an { after the $", reader.getLineNumber() );
				readGStringExpression( reader, writer, multiline );
				continue;
			}

			if( c == quote )
			{
				writer.write( quote );
				if( !multiline )
					return;

				reader.mark( 2 );
				if( reader.read() == quote && reader.read() == quote )
				{
					writer.write( quote );
					writer.write( quote );
					break;
				}

				reader.reset();
				continue;
			}

			writer.write( (char)c );
		}
	}

	private void readGStringExpression( PushbackReader reader, ModalWriter writer, boolean multiline )
	{
		// GStringExpressions can be read in any mode
		// Expecting }, ", ' and {
		// } within a string should not end this expression

		writer.write( '$' );
		writer.write( '{' );
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
				readString( reader, writer, (char)c );
			else if( c =='{' )
				readBlock( reader, writer, multiline );
			else
				writer.write( (char)c );
		}
		writer.write( '}' );
	}

	private void readBlock( PushbackReader reader, ModalWriter writer, boolean multiline )
	{
		// Expecting }, " or '
		// } within a string should not end this block

		writer.write( '{' );
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
				readString( reader, writer, (char)c );
			else if( c =='{' )
				readBlock( reader, writer, multiline );
			else
				writer.write( (char)c );
		}
		writer.write( '}' );
	}

	private void readComment( PushbackReader reader, ModalWriter writer )
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
					break;
				reader.reset();
			}
			else if( c == '<' )
			{
				reader.mark( 10 );
				if( reader.read() == '%' && reader.read() == '-' && reader.read() == '-' )
					readComment( reader, writer );
				else
					reader.reset();
			}
			else if( c == '\n' )
			{
				writer.write( '\n' );
			}
		}
	}

	/**
	 * A writer that writes in different modes. The implementation of this class need to react to mode changes.
	 * 
	 * @author René M. de Bloois
	 */
	static abstract public class ModalWriter
	{
		/**
		 * The different modes that the {@link ModalWriter} should be able to work in.
		 * 
		 * @author René M. de Bloois
		 */
		static public enum Mode
		{
			/**
			 * Script mode: <% %>.
			 */
			SCRIPT,
			/**
			 * Text mode.
			 */
			TEXT,
			/**
			 * An <%= %> expression.
			 */
			EXPRESSION
		}

		/**
		 * The current mode.
		 */
		protected Mode mode = Mode.SCRIPT;

		/**
		 * The next mode. Gets activated when something is writen.
		 */
		protected Mode nextMode = Mode.SCRIPT;

		private StringBuilder buffer = new StringBuilder();

		/**
		 * A directive is encountered.
		 * 
		 * @param name The name of the directive.
		 * @param attribute The attribute name.
		 * @param value The value of the attribute.
		 * @param lineNumber The line number where the directive is encountered.
		 */
		@SuppressWarnings( "unused" )
		protected void directive( String name, String attribute, String value, int lineNumber )
		{
			// Nothing
		}

		/**
		 * Sets the next mode. This mode gets activated when something is written.
		 * 
		 * @param mode The next mode.
		 */
		protected void nextMode( Mode mode )
		{
			this.nextMode = mode;
		}

		/**
		 * Activate the next mode and write the given character.
		 * 
		 * @param c The character to write.
		 */
		protected void write( char c )
		{
			activateMode( this.nextMode );
			this.buffer.append( c );
		}

		/**
		 * Activate the next mode and write the given character sequence.
		 * 
		 * @param string The character sequence to write.
		 */
		protected void write( StringBuilder string ) // StringBuilder.append does strange stuff when given a CharSequence
		{
			if( string == null || string.length() == 0 )
				return;
			activateMode( this.nextMode );
			this.buffer.append( string );
		}

		/**
		 * Directly activate the given mode. Implementors should react to a mode change.
		 * 
		 * @param mode The mode to activate.
		 */
		abstract protected void activateMode( Mode mode );

		/**
		 * Returns the buffer.
		 * 
		 * @return The buffer.
		 */
		protected StringBuilder getBuffer()
		{
			return this.buffer;
		}

		/**
		 * Returns the result.
		 * 
		 * @return The result.
		 */
		abstract protected String getResult();

		/**
		 * Directly append the given character to the buffer.
		 * 
		 * @param c The character to append.
		 */
		protected void append( char c )
		{
			this.buffer.append( c );
		}

		/**
		 * Directly append the given character sequence to the buffer.
		 * 
		 * @param string The character sequence to append.
		 */
		protected void append( CharSequence string )
		{
			this.buffer.append( string );
		}
	}

	static class ScriptWriter extends ModalWriter
	{
		@Override
		protected void activateMode( Mode mode )
		{
			if( this.mode == mode )
				return;
			throw new UnsupportedOperationException();
		}

		@Override
		protected String getResult()
		{
			throw new UnsupportedOperationException();
		}
	}
}
