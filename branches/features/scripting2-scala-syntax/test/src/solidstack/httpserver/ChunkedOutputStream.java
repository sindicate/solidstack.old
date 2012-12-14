/*--
 * Copyright 2012 René M. de Bloois
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

package solidstack.httpserver;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Converts a byte stream into a stream that complies with HTTP's chunked Transfer-Encoding.
 *
 * @author René M. de Bloois
 */
public class ChunkedOutputStream extends OutputStream
{
	/**
	 * The real {@link OutputStream}.
	 */
	protected OutputStream out;
	protected boolean closed;

	/**
	 * Constructor.
	 *
	 * @param out The real {@link OutputStream}.
	 */
	public ChunkedOutputStream( OutputStream out )
	{
		this.out = out;
	}

	@Override
	public void write( int b ) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void write( byte[] b ) throws IOException
	{
		write( b, 0, b.length );
	}

	@Override
	public void write( byte[] b, int off, int len ) throws IOException
	{
		if( len == 0 )
			return;
		this.out.write( Integer.toHexString( len ).getBytes() ); // TODO Give a CharSet here?
		this.out.write( '\r' );
		this.out.write( '\n' );
		this.out.write( b, off, len );
		this.out.write( '\r' );
		this.out.write( '\n' );
	}

	@Override
	public void flush() throws IOException
	{
		// Sorry, flush ain't working on a chunked outputstream
	}

	@Override
	public void close() throws IOException
	{
		if( this.closed )
			return;
		this.out.write( '0' );
		this.out.write( '\r' );
		this.out.write( '\n' );
		this.out.write( '\r' );
		this.out.write( '\n' );
		this.out.close();
		this.closed = true;
	}
}
