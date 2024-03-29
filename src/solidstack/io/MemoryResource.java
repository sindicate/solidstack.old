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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A memory resource.
 *
 * @author Ren� M. de Bloois
 */
public class MemoryResource extends Resource
{
	/**
	 * The buffer containing the resource's bytes.
	 */
	private ByteMatrixOutputStream buffer = new ByteMatrixOutputStream();

	/**
	 * Constructor for an empty memory resource.
	 */
	public MemoryResource()
	{
		// Default constructor
	}

	/**
	 * Constructs a new memory resource with the given bytes.
	 *
	 * @param bytes Bytes to use for the resource.
	 */
	public MemoryResource( byte[] bytes )
	{
		try
		{
			this.buffer.write( bytes );
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	/**
	 * Constructs a new memory resource by reading the input stream to the end.
	 *
	 * @param input The input stream to be read.
	 */
	public MemoryResource( InputStream input )
	{
		append( input );
	}

	// TODO supportsURL does not indicate that this one is re-iterable
	@Override
	public InputStream newInputStream()
	{
		return new ByteMatrixInputStream( this.buffer.toByteMatrix() );
	}

	@Override
	public OutputStream getOutputStream()
	{
		// TODO Should we implement this?
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource resolve( String path )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Appends the contents of the input stream to this memory resource.
	 *
	 * @param input The input stream to be read.
	 */
	public void append( InputStream input )
	{
		byte[] buffer = new byte[ 4096 ];
		int count;
		try
		{
			while( ( count = input.read( buffer ) ) >= 0 )
				this.buffer.write( buffer, 0, count );
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	@Override
	public boolean exists()
	{
		return true;
	}

	@Override
	public long getLastModified()
	{
		// TODO Should this be implemented?
		return 0;
	}
}
