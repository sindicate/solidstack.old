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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * A resource that is located on the classpath.
 *
 * @author René M. de Bloois
 */
public class ClassPathResource extends Resource
{
	/**
	 * The URI of the resource.
	 */
	protected URI uri;

	/**
	 * The class loader used to find the resource.
	 */
	protected ClassLoader classLoader;


	/**
	 * @param path The path of the resource.
	 */
	// TODO Need a classloader parameter?
	public ClassPathResource( String path )
	{
		this( toURI( path ) );
	}

	/**
	 * @param uri The URI of the resource.
	 */
	public ClassPathResource( URI uri )
	{
		String path = uri.getPath();
		if( !"classpath".equals( uri.getScheme() ) )
			throw new IllegalArgumentException( "uri scheme must be 'classpath'" );
		if( path == null ) // If path does not start with / then getPath() returns null
			throw new IllegalArgumentException( "path must start with /" );
		this.uri = uri.normalize();

		this.classLoader = Thread.currentThread().getContextClassLoader();
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
			throw new FatalURISyntaxException( e );
		}
	}

	/**
	 * Always returns true for a ClassPathResource.
	 */
	@Override
	public boolean supportsURL()
	{
		return exists();
	}

	@Override
	public URL getURL()
	{
		URL result = getResource();
		if( result == null )
			throw new UnsupportedOperationException( "File " + toString() + " not found" );
		return result;
	}

	private URL getResource()
	{
		if( this.classLoader != null )
			return this.classLoader.getResource( getPath() );
		return ClassPathResource.class.getClassLoader().getResource( getPath() );
	}

	@Override
	public URI getURI()
	{
		try
		{
			return getURL().toURI();
		}
		catch( URISyntaxException e )
		{
			throw new FatalURISyntaxException( e );
		}
	}

	@Override
	public InputStream newInputStream() throws FileNotFoundException
	{
		URL result = getResource();
		if( result == null )
			throw new FileNotFoundException( "File " + toString() + " not found" );
		try
		{
			return result.openStream();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	// TODO Need test for this
	@Override
	public Resource resolve( String path )
	{
		// FIXME I think this should inherit the class loader
		return Resources.getResource( this.uri.resolve( path ).toString() ); // TODO Test \
	}

	@Override
	public String toString()
	{
		return this.uri.toString();
	}

	@Override
	public boolean exists()
	{
		return getResource() != null;
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

	/**
	 * If the resource exists in the file system a {@link FileResource} is returned. If the resource is not found in a
	 * jar, a {@link URIResource} is returned. Otherwise the resource itself is returned.
	 *
	 * @return A {@link FileResource}, {@link URIResource} or a {@link ClassPathResource}.
	 */
	// TODO Find another way, maybe automatic unwrap when getLastModified() is called, etc. Maybe cache the unwrapped resource.
	@Override
	public Resource unwrap()
	{
		URL url = getResource();
		if( url == null )
			return this;
		if( url.getProtocol().equals( "jar" ) )
			return this;
		try
		{
			if( url.getProtocol().equals( "file" ) )
				return new FileResource( new File( url.toURI() ) );
			return new URIResource( url );
		}
		catch( URISyntaxException e )
		{
			throw new FatalURISyntaxException( e );
		}
	}
}
