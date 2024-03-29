/*--
 * Copyright 2012 Ren� M. de Bloois
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;


/**
 * Represents a resource.
 *
 * @author Ren� de Bloois
 */
public class Resource
{
	private boolean gzip;

	/**
	 * @return True if this resource has a URL and URI, false otherwise.
	 */
	public boolean supportsURL()
	{
		return false;
	}

	/**
	 * @return True if the resource exists, false otherwise.
	 */
	public boolean exists()
	{
		return true;
	}

	/**
	 * @return The last modified time in milliseconds.
	 */
	public long getLastModified()
	{
		return 0;
	}

	/**
	 * @return The URL of this resource.
	 */
	public URL getURL()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The URI of this resource.
	 */
	public URI getURI()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return A new InputStream to read from the resource.
	 * @throws FileNotFoundException If the resource is not found.
	 */
	public InputStream newInputStream() throws FileNotFoundException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return True if {@link #newReader()} is supported, false otherwise.
	 */
	// TODO Rename to ?
	public boolean supportsReader()
	{
		return false;
	}

	/**
	 * @return A new Reader to read from the resource.
	 * @throws FileNotFoundException If the resource is not found.
	 */
	public Reader newReader() throws FileNotFoundException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return An OutputStream to write to the resource.
	 */
	// TODO Rename to newOutputStream?
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException();
	}

    /**
     * Constructs a new resource by resolving the given path against this resource.
     *
     * @param path The path to resolve against this resource.
     * @return The resulting resource.
     */
 	public Resource resolve( @SuppressWarnings( "unused" ) String path )
	{
		throw new UnsupportedOperationException();
	}

 	/**
 	 * Calculates a relative path from the given resource to this resource.
 	 *
 	 * @param base The resource to calculate the relative path from.
 	 * @return The relative path from the given resource to this resource.
 	 */
	// TODO Return String or URI?
	public URI getPathFrom( Resource base )
	{
		return URIResource.relativize( base.getURI(), getURI() );
	}


    /**
     * @return A normalized path to this resource.
     */
	public String getNormalized()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return A resource that better implements the capabilities of the actual resource.
	 */
	public Resource unwrap()
	{
		return this;
	}

	/**
	 * @return The location of the resource.
	 */
	public SourceLocation getLocation()
	{
		return new SourceLocation( this, 1 );
	}

	/**
	 * Indicate that this resource is gzipped or not.
	 *
	 * @param gzip True indicates that this resource is gzipped.
	 */
	public void setGZip( boolean gzip )
	{
		this.gzip = gzip;
	}

	/**
	 * @return True indicates that this resource is gzipped.
	 */
	public boolean isGZip()
	{
		return this.gzip;
	}
}
