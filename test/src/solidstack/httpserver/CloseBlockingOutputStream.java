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

package solidstack.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A decorator that does not pass through calls to the {@link #close()} method. This is used to protect the output stream obtained from the socket.
 *
 * @author Ren� M. de Bloois
 */
public class CloseBlockingOutputStream extends FilterOutputStream
{
	/**
	 * Constructor.
	 *
	 * @param out The real {@link OutputStream}.
	 */
	public CloseBlockingOutputStream( OutputStream out )
	{
		super( out );
	}

	// This one has bad implementation in FilterOutputStream
	@Override
	public void write( byte[] b ) throws IOException
	{
		this.out.write( b );
	}

	// This one has bad implementation in FilterOutputStream
	@Override
	public void write( byte[] b, int off, int len ) throws IOException
	{
		this.out.write( b, off, len );
	}

	@Override
	public void close() throws IOException
	{
		flush(); // But don't close
	}
}
