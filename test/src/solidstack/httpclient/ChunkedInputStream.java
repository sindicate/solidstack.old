package solidstack.httpclient;

import java.io.IOException;
import java.io.InputStream;

import solidstack.httpserver.HttpException;
import solidstack.lang.Assert;

public class ChunkedInputStream extends InputStream
{
	private InputStream in;
	private int left;

	public ChunkedInputStream( InputStream in )
	{
		this.in = in;
	}

	@Override
	public int read() throws IOException
	{
		if( this.left > 0 )
		{
			int result = this.in.read();
			this.left--;
			if( this.left == 0 )
			{
				int ch = this.in.read();
				if( ch == '\r' )
					ch = this.in.read();
				Assert.isTrue( ch == '\n' );
			}
			return result;
		}

		if( this.left == -1 )
			return -1;

		String line = readLine();
		this.left = Integer.parseInt( line, 16 );
		if( this.left == 0 )
		{
			int ch = this.in.read();
			if( ch == '\r' )
				ch = this.in.read();
			Assert.isTrue( ch == '\n' );
			this.left = -1;
			return -1;
		}

		return read();
	}

	private String readLine() throws IOException
	{
		StringBuilder result = new StringBuilder();
		while( true )
		{
			int ch = this.in.read();
			if( ch == -1 )
				throw new HttpException( "Unexpected end of line" );
			if( ch == '\r' )
				continue;
			if( ch == '\n' )
				return result.toString();
			result.append( (char)ch );
		}
	}
}
