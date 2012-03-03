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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * A resource that is located in the classpath.
 *
 * @author René M. de Bloois
 */
public class ClassPathResource extends Resource
{
	/**
	 * The path of the resource.
	 */
	protected URI uri;

	/**
	 * Constructor.
	 *
	 * @param path The path of the resource.
	 */
	// TODO Need a classloader too
	public ClassPathResource( String path )
	{
		this( toURI( path ) );
	}

	public ClassPathResource( URI uri )
	{
		String path = uri.getPath();
		if( path == null ) // If path does not start with / then getPath() returns null
			throw new IllegalArgumentException( "path must start with /" );
		if( !"classpath".equals( uri.getScheme() ) )
			throw new IllegalArgumentException( "uri scheme must be 'classpath'" );
		this.uri = uri.normalize();
	}

	static private URI toURI( String path )
	{
		try
		{
			if( path.startsWith( "classpath:" ) )
				return new URI( path );
			return new URI( "classpath:" + path );
		}
		catch( URISyntaxException e )
		{
			throw new FatalIOException( e );
		}
	}

	/**
	 * Always returns true.
	 */
	@Override
	public boolean supportsURL()
	{
		return true;
	}

	/**
	 * Returns the URL for this resource.
	 *
	 * @throws FileNotFoundException When a file is not found.
	 */
	@Override
	public URL getURL() throws FileNotFoundException
	{
		URL result = ClassPathResource.class.getClassLoader().getResource( getPath() );
		if( result == null )
			throw new FileNotFoundException( "File " + toString() + " not found" );
		return result;
	}

	/**
	 * Returns an InputStream for this resource.
	 *
	 * @throws FileNotFoundException When a file is not found.
	 */
	@Override
	public InputStream getInputStream() throws FileNotFoundException
	{
		InputStream result = ClassPathResource.class.getClassLoader().getResourceAsStream( getPath() );
		if( result == null )
			throw new FileNotFoundException( "File " + toString() + " not found" );
		return result;
	}

	// TODO Need test for this
	@Override
	public Resource resolve( String path )
	{
		return ResourceFactory.getResource( this.uri.resolve( path ).toString() ); // TODO Test \
	}

	@Override
	public String toString()
	{
		return this.uri.toString();
	}

	@Override
	public boolean exists()
	{
		return ClassPathResource.class.getClassLoader().getResource( getPath() ) != null;
	}

	// A resource in the class path cannot start with a /
	private String getPath()
	{
		return this.uri.getPath().substring( 1 );
	}

	@Override
	public long getLastModified()
	{
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
		URL url = ClassPathResource.class.getClassLoader().getResource( getPath() );
		if( url == null )
			return this; // TODO Or file not found?
		if( url.getProtocol().equals( "jar" ) )
			return this;
		if( url.getProtocol().equals( "file" ) )
			try
			{
				return new FileResource( new File( url.toURI() ) );
			}
			catch( URISyntaxException e )
			{
				throw new FatalIOException( e );
			}
		return new URLResource( url );
	}
}
