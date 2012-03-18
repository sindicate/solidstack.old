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

package solidstack.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;


/**
 * A writer that does not write to the given resource until a certain threshold is reached.
 *
 * @author René de Bloois
 */
public class DeferringWriter extends Writer
{
	private int threshold;
	private Resource resource;
	private String encoding;

	private StringBuilder buffer;
	private Writer writer;

	/**
	 * @param threshold The threshold.
	 * @param resource The resource to write to.
	 * @param encoding The character encoding to use.
	 * @throws UnsupportedEncodingException When the encoding is not supported.
	 */
	public DeferringWriter( int threshold, Resource resource, String encoding ) throws UnsupportedEncodingException
	{
		if( !Charset.isSupported( encoding ) )
			throw new UnsupportedEncodingException( encoding );

		this.threshold = threshold;
		this.resource = resource;
		this.encoding = encoding;

		if( threshold > 0 )
			this.buffer = new StringBuilder();
	}

	private void initWriter()
	{
		try
		{
			this.writer = new OutputStreamWriter( this.resource.getOutputStream(), this.encoding );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new IllegalStateException( e ); // This can't happen
		}
	}

	@Override
	public void write( int c ) throws IOException
	{
		if( this.buffer != null )
		{
			this.buffer.append( (char)c );
			if( this.buffer.length() >= this.threshold )
			{
				initWriter();
				this.writer.write( clearBuffer() );
			}
		}
		else
		{
			if( this.writer == null )
				initWriter();
			this.writer.write( c );
		}
	}

	@Override
	public void write( char[] cbuf, int off, int len ) throws IOException
	{
		if( this.buffer != null )
			if( this.buffer.length() + len >= this.threshold )
			{
				initWriter();
				this.writer.write( clearBuffer() );
				this.writer.write( cbuf, off, len );
			}
			else
				this.buffer.append( cbuf, off, len );
		else
		{
			if( this.writer == null )
				initWriter();
			this.writer.write( cbuf, off, len );
		}
	}

	/**
	 * @return True when the characters are still in the buffer.
	 */
	public boolean isBuffered()
	{
		return this.buffer != null;
	}

	/**
	 * Returns the characters from the buffer and clears the buffer.
	 *
	 * @return The characters from the buffer.
	 */
	public String clearBuffer()
	{
		String result = this.buffer.toString();
		this.buffer = null;
		return result;
	}

	@Override
	public void flush() throws IOException
	{
		if( isBuffered() )
			throw new IllegalStateException( "Characters are still in the buffer" );
		if( this.writer != null )
			this.writer.flush();
	}

	@Override
	public void close() throws IOException
	{
		if( isBuffered() )
			throw new IllegalStateException( "Characters are still in the buffer" );
		if( this.writer != null )
			this.writer.close();
	}
}
