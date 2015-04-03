package solidstack.httpserver;

import java.io.IOException;
import java.io.InputStream;

import solidstack.lang.Assert;


public class HttpBodyInputStream extends InputStream
{
	private InputStream in;
	private int length; // TODO long?
	private int read;

	public HttpBodyInputStream( InputStream in, int length )
	{
		Assert.notNull( in );
		this.in = in;
		this.length = length;
	}

	@Override
	public int read() throws IOException
	{
		if( this.read >= this.length )
			return -1;
		int result = this.in.read();
		Assert.isTrue( result >= 0 );
		this.read++;
		return result;
	}
}
