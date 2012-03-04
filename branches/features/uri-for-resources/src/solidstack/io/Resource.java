package solidstack.io;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;


/**
 * Represents a resource.
 *
 * @author René de Bloois
 */
public class Resource
{
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
	 *
	 * @throws FileNotFoundException If the resource is not found.
	 */
	// TODO Don't throw FileNotFoundException
	public URL getURL() throws FileNotFoundException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The URI of this resource.
	 *
	 * @throws FileNotFoundException If the resource is not found.
	 */
	// TODO Don't throw FileNotFoundException
	public URI getURI() throws FileNotFoundException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return An InputStream to read from the resource.
	 *
	 * @throws FileNotFoundException If the resource is not found.
	 */
	public InputStream getInputStream() throws FileNotFoundException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return An OutputStream to write to the resource.
	 */
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
 	 * @param other The resource to calculate the relative path from.
 	 * @return The relative path from the given resource to this resource.
 	 */
	public String getPathFrom( @SuppressWarnings( "unused" ) Resource other )
	{
		throw new UnsupportedOperationException();
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
}
