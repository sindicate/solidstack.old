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


/**
 * My own PushbackReader. This one has an unlimited buffer and maintains the current line number. It also gives access
 * to the underlying reader. Furthermore, it wraps {@link IOException}s in a {@link RuntimeException} because we are
 * never interested in it.
 *
 * @author René M. de Bloois
 */
public class PushbackReader
{
	/**
	 * The underlying reader.
	 */
	private SourceReader reader;

	/**
	 * The push back buffer;
	 */
	private StringBuilder buffer = new StringBuilder();

	/**
	 * The current line number.
	 */
	private int lineNumber;

	/**
	 * Needed for {@link #mark(int)} and {@link #reset()}.
	 */
	private StringBuilder markBuffer;


	/**
	 * @param reader A source reader.
	 */
	public PushbackReader( SourceReader reader )
	{
		this.reader = reader;
		this.lineNumber = reader.getLineNumber();
	}

	/**
	 * @return The current line number.
	 */
	public int getLineNumber()
	{
		return this.lineNumber;
	}

	/**
	 * @return The current location in the source.
	 */
	public SourceLocation getLocation()
	{
		SourceLocation result = this.reader.getLocation();
		if( result.getLineNumber() != this.lineNumber )
			throw new IllegalStateException( "There is a newline in the push back buffer" );
		return result;
	}

	public SourceLocation getLastLocation()
	{
		if( this.buffer.length() > 0 )
			throw new IllegalStateException( "There are still characters in the push back buffer" );
		return this.reader.getLastLocation();
	}

	/**
	 * Returns the underlying reader. But only if the back buffer is empty, otherwise an IllegalStateException is thrown.
	 *
	 * @return The underlying reader.
	 */
	public SourceReader getReader()
	{
		if( this.buffer.length() > 0 )
			throw new IllegalStateException( "There are still characters in the push back buffer" );
		return this.reader;
	}

	/**
	 * @return The resource that is being read.
	 */
	public Resource getResource()
	{
		return this.reader.getResource();
	}

	/**
	 * Read one character. If the buffer contains characters, the character is taken from there. If the buffer is empty,
	 * the character is taken from the underlying reader. Carriage returns are filtered out. \r and \r\n are
	 * automatically translated to a single \n. The current line number is incremented for each newline encountered.
	 *
	 * @return The character read or -1 if no more characters are available.
	 */
	public int read()
	{
		int result;

		if( this.buffer.length() > 0 )
		{
			int p = this.buffer.length() - 1;
			result = this.buffer.charAt( p );
			this.buffer.delete( p, p + 1 ); // No cost involved, deleting from the end only decrements a count
		}
		else
		{
			result = this.reader.read(); // No \r returned by the SourceReader
		}

		if( result == '\n' )
			this.lineNumber++;

		if( this.markBuffer != null )
		{
			if( this.markBuffer.length() == this.markBuffer.capacity() ) // TODO May need unit test for this, or do it differently
				this.markBuffer = null; // Reached limit
			else
				this.markBuffer.append( (char)result );
		}

		return result;
	}

	/**
	 * Push a character back into the reader. The current line number is decremented when a newline character is pushed back.
	 *
	 * @param ch The character to push back. -1 is ignored.
	 */
	public void push( int ch )
	{
		if( ch == '\r' )
			throw new IllegalArgumentException( "CR's can't be pushed back into the reader" );
		if( ch != -1 )
		{
			if( ch == '\n' )
				this.lineNumber--;
			this.buffer.append( (char)ch );
	}
	}

	/**
	 * Push a complete {@link CharSequence} back into the reader. The current line number is decremented for each newline encountered.
	 *
	 * @param chars The {@link CharSequence} to push back.
	 */
	public void push( CharSequence chars )
	{
		int len = chars.length();
		while( len > 0 )
			push( chars.charAt( --len ) ); // Use push to decrement the line number when a \n is found
	}

    /**
     * Marks the current position in the stream.
     *
     * @param maxLength If characters are read beyond the limit, the mark is lost.
     */
	public void mark( int maxLength )
	{
		this.markBuffer = new StringBuilder( maxLength );
	}

	/**
	 * Resets the reader to the mark.
	 */
	public void reset()
	{
		if( this.markBuffer == null )
			throw new FatalIOException( "The mark is lost or no mark has been set" );
		push( this.markBuffer );
		this.markBuffer = null;
	}

	/**
	 * Close the reader.
	 */
	public void close()
	{
		this.reader.close();
	}
}
