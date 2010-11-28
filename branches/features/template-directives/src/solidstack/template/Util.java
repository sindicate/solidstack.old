package solidstack.template;

import java.io.StringReader;

import solidstack.Assert;
import solidstack.io.PushbackReader;

public class Util
{
	static public String getToken( PushbackReader reader )
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
		while( ch != -1 && !Character.isWhitespace( ch ) && ch != '=' );

		// Push back the last character
		reader.push( ch );

		// Return the result
		Assert.isFalse( result.length() == 0 );
		return result.toString();
	}

	static public Directive parseDirective( String directive, int lineNumber )
	{
		PushbackReader reader = new PushbackReader( new StringReader( directive ), lineNumber );
		String category = getToken( reader );
		if( category == null )
			throw new ParseException( "Syntax error in directive", lineNumber );
		String property = getToken( reader );
		if( property == null )
			throw new ParseException( "Syntax error in directive", lineNumber );
		if( !getToken( reader ).equals( "=" ) )
			throw new ParseException( "Syntax error in directive", lineNumber );
		String value = getToken( reader );
		if( value == null || !value.startsWith( "\"" ) || !value.endsWith( "\"" ) )
			throw new ParseException( "Syntax error in directive", lineNumber );
		value = value.substring( 1, value.length() - 2 );

		return new Directive( category, property, value );
	}
}
