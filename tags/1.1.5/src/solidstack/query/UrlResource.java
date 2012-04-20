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

package solidstack.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import solidstack.SystemException;

/**
 * A resource described by a {@link URL}.
 * 
 * @author René M. de Bloois
 */
public class UrlResource
{
	private URL url;

	/**
	 * The constructor.
	 * 
	 * @param url The {@link URL} of the resource.
	 */
	public UrlResource( URL url )
	{
		if( url == null )
			throw new IllegalArgumentException( "url should not be null" );
		this.url = url;
	}

	/**
	 * Returns a {@link File} for the resource.
	 * 
	 * @return a {@link File} for the resource.
	 * @throws FileNotFoundException When the url protocol is not 'file'.
	 */
	public File getFile() throws FileNotFoundException
	{
		if( !this.url.getProtocol().equals( "file" ) )
			throw new FileNotFoundException( "Only file: protocol allowed, not " + this.url.getProtocol() );
		try
		{
			System.out.println( this.url );
			return new File( this.url.toURI() );
		}
		catch( URISyntaxException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Returns an {@link InputStream} for the resource.
	 * 
	 * @return an {@link InputStream} for the resource.
	 */
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

	/**
	 * Determines if the resource exists.
	 * 
	 * @return True if the resource exists, false otherwise.
	 */
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

	/**
	 * Determines the last modified time stamp of the resource. Return 0 if the last modified time stamp is not available.
	 * 
	 * @return The last modified time stamp of the resource. 0 if the last modified time stamp is not available.
	 */
	public long getLastModified()
	{
		try
		{
			return getFile().lastModified();
		}
		catch( FileNotFoundException e )
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return this.url.toString();
	}
}
