package solidstack.template;

import solidstack.Assert;
import solidstack.io.PushbackReader;
import solidstack.template.JSPLikeTemplateParser.Writer.Mode;


public class JSPLikeTemplateParser
{
//	public JSPLikeTemplateParser( PushbackReader reader, Writer writer, String pkg, String cls )
//	{
//
//	}

	public String parse( PushbackReader reader, Writer writer )
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
					Writer buffer = writer.newWriter( Mode.SCRIPT );
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
				// TODO And without {}?
				int cc = reader.read();
				if( cc == '{' )
					readGStringExpression( reader, writer );
				else
				{
					writer.write( (char)c );
					reader.push( cc );
				}
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
		writer.nextMode( Mode.SCRIPT );
		writer.write( "return builder.toGString()}}}" );

		return writer.getString();
	}

	private void readDirective( PushbackReader reader, Writer writer )
	{
		// We are in DIRECTIVE mode here.
		// Expecting only %>

		int lineNumber = reader.getLineNumber();
		StringBuilder directive = new StringBuilder();
		while( true )
		{
			int c = reader.read();
			if( c < 0 )
				throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			if( c == '%' )
			{
				int cc = reader.read();
				if( cc == '>' )
					break;
				reader.push( cc );
			}
			directive.append( (char)c );
		}

		writer.directive( directive.toString(), lineNumber );
	}

	protected StringBuilder readWhitespace( PushbackReader reader )
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

	protected void readScript( PushbackReader reader, Writer writer )
	{
		Assert.isTrue( writer.nextMode == Mode.SCRIPT || writer.nextMode == Mode.EXPRESSION );

		// We are in SCRIPT/EXPRESSION mode here.
		// Expecting ", %>
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

	protected void readString( PushbackReader reader, Writer writer, char quote )
	{
		Assert.isTrue( writer.nextMode == Mode.EXPRESSION || writer.nextMode == Mode.SCRIPT || writer.nextMode == Mode.TEXT, "Unexpected mode " + writer.nextMode );

		// String can be read in any mode
		// Expecting $, " and \
		// " within ${} should not end this string
		// \ is used to escape $, " and itself

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

			if( multiline )
			{
				if( c < 0 )
					throw new ParseException( "Unexpected end of file", reader.getLineNumber() );
			}
			else
				if( c < 0 || c == '\n' )
					throw new ParseException( "Unexpected end of line", reader.getLineNumber() );

			if( c == '\\' )
			{
				writer.write( (char)c );
				c = reader.read();
				if( c == '$' && quote == '"' || c == '\\' || c == quote  )
					writer.write( (char)c );
				else
					throw new ParseException( "Only " + ( quote == '"' ? "\", $" : "'" ) + " or \\ can be escaped", reader.getLineNumber() );
				continue;
			}

			if( quote == '"' && c == '$' )
			{
				// TODO And without {}?
				c = reader.read();
				if( c == '{' )
					readGStringExpression( reader, writer );
				else
				{
					writer.write( '$' );
					reader.push( c );
				}
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

	// TODO Should we allow { } blocks within GString expressions? This works in expressions: <%= { prefix }.call() %>
	protected void readGStringExpression( PushbackReader reader, Writer writer )
	{
		// GStringExpressions can be read in any mode
		// Expecting }, "
		// } within a string should not end this expression

		writer.write( '$' );
		writer.write( '{' );
		while( true )
		{
			int c = reader.read();
			if( c < 0 || c == '\n' )
				throw new ParseException( "Unexpected end of line", reader.getLineNumber() );
			if( c == '}' )
				break;
			if( c == '"' || c == '\'' )
				readString( reader, writer, (char)c );
			else
				writer.write( (char)c );
		}
		writer.write( '}' );
	}

	protected void readComment( PushbackReader reader, Writer writer )
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

	static abstract public class Writer
	{
		static public enum Mode { INITIAL, SCRIPT, TEXT, EXPRESSION }

		protected StringBuilder buffer = new StringBuilder();
		protected Mode mode = Mode.INITIAL;
		protected Mode nextMode = Mode.INITIAL;

		protected Writer()
		{
			// Nothing
		}

		protected void directive( String directive, int lineNumber )
		{
			// Nothing
		}

		protected Writer( Mode mode )
		{
			this.mode = this.nextMode = mode;
		}

		protected void nextMode( Mode mode )
		{
			this.nextMode = mode;
		}

		protected void write( char c )
		{
			switchMode( this.nextMode );
			this.buffer.append( c );
		}

		protected void write( CharSequence string )
		{
			if( string == null || string.length() == 0 )
				return;
			switchMode( this.nextMode );
			this.buffer.append( string );
		}

		abstract protected void switchMode( Mode mode );

		protected StringBuilder getBuffer()
		{
			return this.buffer;
		}

		protected String getString()
		{
			return this.buffer.toString();
		}

//		protected StringBuilder switchBuffer( StringBuilder buffer )
//		{
//			StringBuilder result = this.buffer;
//			this.buffer = buffer;
//			return result;
//		}

		abstract protected Writer newWriter( Mode mode );
	}
}
