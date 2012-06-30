package solidstack.io;

import java.io.FileNotFoundException;


/**
 * A resource that remembers the original resource so that {@link #resolve(String)} and {@link #getLastModified()} keep working.
 *
 * @author René de Bloois
 */
public class BufferedResource extends MemoryResource
{
	private Resource resource;

	/**
	 * @param resource The resource to buffer in memory.
	 * @throws FileNotFoundException If the resource throws it when retrieving an input stream.
	 */
	public BufferedResource( Resource resource ) throws FileNotFoundException
	{
		this.resource = resource;
		append( resource.newInputStream() );
	}

	@Override
	public Resource resolve( String path )
	{
		return this.resource.resolve( path );
	}

	@Override
	public long getLastModified()
	{
		return this.resource.getLastModified();
	}
}
