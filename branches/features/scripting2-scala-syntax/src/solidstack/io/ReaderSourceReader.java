/*--
 * Copyright 2010 René M. de Bloois
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
import java.io.Reader;


/**
 * A source reader that reads from a reader.
 *
 * @author René M. de Bloois
 */
public class ReaderSourceReader implements SourceReader
{
	/**
	 * The reader used to read from.
	 */
	private Reader reader;

	/**
	 * The current line the reader is positioned on.
	 */
	private int lineNumber;

	/**
	 * The current location.
	 */
	private SourceLocation location;

	/**
	 * Buffer to contain a character that has been read by mistake.
	 */
	private int buffer = -1;

	/**
	 * Buffer to contain the line that is being read.
	 */
	private StringBuilder line;

	/**
	 * The underlying resource.
	 */
	private Resource resource;

	/**
	 * The character encoding of the resource.
	 */
	private String encoding;


	/**
	 * @param reader The reader to read from.
	 */
	public ReaderSourceReader( Reader reader )
	{
		this.reader = reader;
		this.lineNumber = 1;
	}

	/**
	 * @param reader The reader to read from.
	 * @param location The location.
	 */
	public ReaderSourceReader( Reader reader, SourceLocation location )
	{
		this.reader = reader;
		this.resource = location.getResource();
		this.lineNumber = location.getLineNumber();
	}

	/**
	 * @param reader The reader to read from.
	 * @param location The location.
	 * @param encoding The encoding used.
	 */
	public ReaderSourceReader( Reader reader, SourceLocation location, String encoding )
	{
		this.reader = reader;
		this.resource = location.getResource();
		this.lineNumber = location.getLineNumber();
		this.encoding = encoding;
	}

	/**
	 * Close the reader and the underlying reader.
	 */
	public void close()
	{
		if( this.reader == null )
			return;

		try
		{
			this.reader.close();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}

		this.reader = null;
	}

	public String readLine()
	{
		if( this.line == null )
			this.line = new StringBuilder();

		int ch;
		while( true )
			switch( ch = read() )
			{
				case -1:
					if( this.line.length() == 0 )
						return null;
					//$FALL-THROUGH$
				case '\n':
					String result = this.line.toString();
					this.line.setLength( 0 );
					return result;
				default:
					this.line.append( (char)ch );
			}
	}

	public int getLineNumber()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );
		return this.lineNumber;
	}

	public int read()
	{
		try
		{
			int result;
			if( this.buffer >= 0 )
			{
				result = this.buffer;
				this.buffer = -1;
			}
			else
				result = this.reader.read();

			switch( result )
			{
				case '\r':
					result = this.reader.read();
					if( result != '\n' )
						this.buffer = result;
					//$FALL-THROUGH$
				case '\n':
					this.lineNumber++;
					return '\n';
				default:
					return result;
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	public Resource getResource()
	{
		return this.resource;
	}

	public String getEncoding()
	{
		return this.encoding;
	}

	public SourceLocation getLocation()
	{
		if( this.location != null && this.location.getLineNumber() == this.lineNumber )
			return this.location;
		return new SourceLocation( this.resource, this.lineNumber );
	}

	void push( int ch )
	{
		if( this.buffer != -1 )
			throw new IllegalStateException( "buffer is not empty" );
		this.buffer = ch;
	}
}
