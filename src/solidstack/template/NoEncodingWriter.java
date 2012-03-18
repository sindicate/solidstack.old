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

package solidstack.template;

import java.io.IOException;
import java.io.Writer;

/**
 * An encoding writer that passes through everything unmodified.
 *
 * @author Ren� M. de Bloois
 */
public class NoEncodingWriter implements EncodingWriter
{
	/**
	 * The writer to write to.
	 */
	protected Writer out;

	/**
	 * Constructor.
	 *
	 * @param out The writer to write to.
	 */
	public NoEncodingWriter( Writer out )
	{
		this.out = out;
	}

	/**
	 * Writes the given characters.
	 *
	 * @param chars The characters.
	 * @param off The start index.
	 * @param len The length.
	 * @throws IOException Whenever an IOException is thrown.
	 */
	public void write( char[] chars, int off, int len ) throws IOException
	{
		this.out.write( chars, off, len );
	}

	public void write( String s ) throws IOException
	{
		if( s != null )
			write( s.toCharArray(), 0, s.length() );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		write( (String)o );
	}

	public boolean stringsOnly()
	{
		return true;
	}
}
