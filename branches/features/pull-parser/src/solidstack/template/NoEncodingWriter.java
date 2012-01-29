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

package solidstack.template;

import java.io.IOException;
import java.io.Writer;

/**
 * An encoding writer. Adds a {@link #writeEncoded(String)} method. This implementation does not encode.
 * 
 * @author René M. de Bloois
 *
 */
// Can't implement Writer. DefaultGroovyMethods.write(Writer self, Writable writable) will be called when value is null, which results in NPE.
public class NoEncodingWriter implements EncodingWriter
{
	/**
	 * The writer to write to.
	 */
	protected Writer writer;

	/**
	 * Constructor.
	 * 
	 * @param writer The writer to write to.
	 */
	public NoEncodingWriter( Writer writer )
	{
		this.writer = writer;
	}

	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void write( String s ) throws IOException
	{
		if( s == null )
			return;

		this.writer.write( s );
	}

	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void writeEncoded( String s ) throws IOException
	{
		if( s == null )
			return;

		write( s );
	}
}
