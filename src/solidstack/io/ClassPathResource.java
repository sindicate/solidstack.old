/*--
 * Copyright 2011 Ren� M. de Bloois
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
 * A resource that is located on the classpath.
 *
 * @author Ren� M. de Bloois
 */
public class ClassPathResource extends Resource
{
	/**
	 * The URI of the resource.
	 */
	protected URI uri;

	/**
	 * @param path The path of the resource.
	 */
	// TODO Need a classloader too
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
			throw new FatalURISyntaxException( e );
		}
	}

	/**
	 * Always returns true for a ClassPathResource.
	 */
	@Override
	public boolean supportsURL()
	{
		return true;
	}

	@Override
	public URL getURL() throws FileNotFoundException
	{
		URL result = ClassPathResource.class.getClassLoader().getResource( getPath() );
		if( result == null )
			throw new FileNotFoundException( "File " + toString() + " not found" );
		return result;
	}

	@Override
	public URI getURI() throws FileNotFoundException
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

	/**
	 * If the resource exists in the file system a {@link FileResource} is returned. If the resource is not found in a
	 * jar, a {@link URIResource} is returned. Otherwise the resource itself is returned.
	 *
	 * @return A {@link FileResource}, {@link URIResource} or a {@link ClassPathResource}.
	 */
	@Override
	public Resource unwrap()
	{
		URL url = ClassPathResource.class.getClassLoader().getResource( getPath() );
		if( url == null )
			return this; // TODO Or file not found?
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
