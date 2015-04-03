package solidstack.httpserver;

import solidstack.io.PushbackReader;

public class UrlEncodedParser
{
	private PushbackReader in;
	private int length;
	private int pos;

	public UrlEncodedParser( PushbackReader in, int length )
	{
		this.in = in;
		this.length = length;
	}

	public String getParameter()
	{
		if( this.pos >= this.length )
			return null;

		StringBuilder result = new StringBuilder();
		while( true )
		{
			if( this.pos >= this.length )
				return result.toString();

			int ch = this.in.read();
			this.pos++;
			if( ch == '=' )
				return result.toString();

			result.append( (char)ch );
		}
	}

	public String getValue()
	{
		if( this.pos >= this.length )
			return null;

		StringBuilder result = new StringBuilder();
		while( true )
		{
			if( this.pos >= this.length )
				return result.toString();

			int ch = this.in.read();
			this.pos++;
			if( ch == '&' )
				return result.toString();

			result.append( (char)ch );
		}
	}
}
