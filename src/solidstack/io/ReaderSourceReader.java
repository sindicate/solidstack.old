/*--
 * Copyright 2010 Ren� M. de Bloois
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
 * @author Ren� M. de Bloois
 */
public class ReaderSourceReader implements SourceReader
{
	/**
	 * The reader used to read from.
	 */
	private Reader reader;

	/**
	 * The current location.
	 */
	private SourceLocation location;

	/**
	 * The last location.
	 */
	private SourceLocation lastLocation;

	/**
	 * Buffer to contain a character that has been read by mistake.
	 */
	private int buffer = -1;

	/**
	 * Buffer to contain the line that is being read.
	 */
	private StringBuilder line;

	/**
	 * The character encoding of the resource.
	 */
	private String encoding;


	/**
	 * @param reader The reader to read from.
	 */
	public ReaderSourceReader( Reader reader )
	{
		this( reader, new SourceLocation( null, 1 ), null );
	}

	/**
	 * @param reader The reader to read from.
	 * @param location The location.
	 */
	public ReaderSourceReader( Reader reader, SourceLocation location )
	{
		this( reader, location, null );
	}

	/**
	 * @param reader The reader to read from.
	 * @param location The location.
	 * @param encoding The encoding used.
	 */
	public ReaderSourceReader( Reader reader, SourceLocation location, String encoding )
	{
		this.reader = reader;
		this.location = location;
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
		this.line.setLength( 0 );

		int ch;
		while( true )
			switch( ch = read() )
			{
				case -1:
					if( this.line.length() == 0 )
						return null;
					this.location = this.location.nextLine(); // Not incremented by read(), so do it here
					//$FALL-THROUGH$
				case '\n':
					return this.line.toString();
				default:
					this.line.append( (char)ch );
			}
	}

	public int getLineNumber()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );
		return this.location.getLineNumber();
	}

	public int read()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );

		this.lastLocation = this.location;
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
					this.location = this.location.nextLine();
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
		return this.location.getResource();
	}

	public String getEncoding()
	{
		return this.encoding;
	}

	public SourceLocation getLocation()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );
		return this.location;
	}

	public SourceLocation getLastLocation()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );
		if( this.lastLocation == null )
			throw new IllegalStateException( "No character read yet" );
		if( this.buffer >= 0 )
			throw new IllegalStateException( "Last location is not valid, pushed back a character" );
		return this.lastLocation;
	}

	// This is only used to push back the first character (for byte order mark detection)
	void push( int ch )
	{
		if( this.buffer != -1 )
			throw new IllegalStateException( "Buffer is not empty" );
		this.buffer = ch;
	}
}
