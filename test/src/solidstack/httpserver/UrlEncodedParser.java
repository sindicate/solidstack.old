package solidstack.httpserver;

import java.io.IOException;
import java.io.InputStream;

import solidstack.io.FatalIOException;

public class UrlEncodedParser
{
	private InputStream in;
	private int length;
	private int pos;

	public UrlEncodedParser( InputStream in, int length )
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

			int ch;
			try
			{
				ch = this.in.read();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
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

			int ch;
			try
			{
				ch = this.in.read();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
			this.pos++;
			if( ch == '&' )
				return result.toString();

			result.append( (char)ch );
		}
	}
}
