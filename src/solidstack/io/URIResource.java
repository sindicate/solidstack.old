/*--
 * Copyright 2011 René M. de Bloois
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

package solidstack.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * A resource identified by a URI.
 *
 * @author René M. de Bloois
 */
public class URIResource extends Resource
{
	/**
	 * The URI.
	 */
	protected URI uri;


	/**
	 * @param uri The URI.
	 */
	public URIResource( URI uri )
	{
		this.uri = uri;
	}

	/**
	 * @param uri The URI.
	 */
	public URIResource( String uri )
	{
		this( toURI( uri ) );
	}

	/**
	 * @param url A URL.
	 */
	public URIResource( URL url )
	{
		this( toURI( url ) );
	}

	static private URI toURI( String uri )
	{
		try
		{
			return new URI( uri );
		}
		catch( URISyntaxException e )
		{
			throw new FatalURISyntaxException( e );
		}
	}

	static private URI toURI( URL url )
	{
		try
		{
			return url.toURI();
		}
		catch( URISyntaxException e )
		{
			throw new FatalURISyntaxException( e );
		}
	}

	@Override
	public boolean supportsURL()
	{
		return true;
	}

	@Override
	public URL getURL()
	{
		try
		{
			return this.uri.toURL();
		}
		catch( MalformedURLException e )
		{
			throw new FatalIOException( e );
		}
	}

	@Override
	public URI getURI()
	{
		return this.uri;
	}

	@Override
	public InputStream newInputStream() throws FileNotFoundException
	{
		try
		{
			return this.uri.toURL().openStream();
		}
		catch( FileNotFoundException e )
		{
			throw e;
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	@Override
	public Resource resolve( String path )
	{
		// TODO Unit test with folder url
		return new URIResource( this.uri.resolve( path ) );
	}

	@Override
	public String toString()
	{
		return this.uri.toString();
	}

	@Override
	public boolean exists()
	{
		// TODO This should be implemented I think
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified()
	{
		// TODO This should be implemented I think
		return 0;
	}

	@Override
	public String getNormalized()
	{
		return this.uri.normalize().toString();
	}

	@Override
	public Resource unwrap()
	{
		if( "file".equals( this.uri.getScheme() ) )
			return new FileResource( new File( this.uri ) );
		return this;
	}
}
