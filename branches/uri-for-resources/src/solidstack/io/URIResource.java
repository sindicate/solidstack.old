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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A resource identified by a URL.
 *
 * @author René M. de Bloois
 */
// TODO Maybe we should use URIResource. That one has no problems with the classpath scheme.
public class URIResource extends Resource
{
	/**
	 * The URL.
	 */
	protected URI uri;

	/**
	 * Constructor.
	 *
	 * @param url The URL.
	 */
	public URIResource( URI url )
	{
		this.uri = url;
	}

	/**
	 * Constructor.
	 *
	 * @param url The URL.
	 * @throws URISyntaxException
	 */
	public URIResource( String uri )
	{
		this( toURI( uri ) );
	}

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
	public URI getURI() throws FileNotFoundException
	{
		return this.uri;
	}

	@Override
	public InputStream getInputStream()
	{
		try
		{
			return this.uri.toURL().openStream();
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
		// TODO The resource factory has more logic then this
		return new URIResource( this.uri.resolve( path ) );
	}

	static String getScheme( String path )
	{
		// scheme starts with a-zA-Z, and contains a-zA-Z0-9 and $-_@.&+- and !*"'(), and %
		Pattern pattern = Pattern.compile( "^([a-zA-Z][a-zA-Z0-9$_@.&+\\-!*\"'(),%]*):" );
		Matcher matcher = pattern.matcher( path );
		if( matcher.find() )
			return matcher.group( 1 );
		return null;
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
		URL url = getURL();
		if( url.getProtocol().equals( "file" ) )
			try
			{
				return new FileResource( new File( url.toURI() ) );
			}
			catch( URISyntaxException e )
			{
				throw new FatalURISyntaxException( e );
			}
		return this;
	}
}
