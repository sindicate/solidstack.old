package solidstack.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlResource
{
	protected URL url;

	public UrlResource( URL url )
	{
		if( url == null )
			throw new IllegalArgumentException( "url should not be null" );
		this.url = url;
	}

	public File getFile() throws FileNotFoundException
	{
//		if( !this.url.getProtocol().equals( "file" ) )
//			throw new FileNotFoundException( "Only file: protocol allowed, not " + this.url.getProtocol() );
		try
		{
			return new File( this.url.toURI() );
		}
		catch( URISyntaxException e )
		{
			throw new SystemException( e );
		}
	}

	public InputStream getInputStream()
	{
		try
		{
			return this.url.openStream();
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public boolean exists()
	{
		try
		{
			// This is more efficient than opening the stream.
			return getFile().exists();
		}
		catch( FileNotFoundException e )
		{
			InputStream in = getInputStream();
			if( in != null )
			{
				try
				{
					in.close();
				}
				catch( IOException e1 )
				{
					throw new SystemException( e );
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "UrlResource(" + this.url + ")";
	}
}
