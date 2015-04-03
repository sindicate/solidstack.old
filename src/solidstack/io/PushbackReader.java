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

import solidstack.SystemException;


/**
 * My own PushbackReader. This one has an unlimited buffer and maintains the current line number. It also gives access
 * to the underlying reader. Furthermore, it wraps {@link IOException}s in a {@link SystemException} because we are
 * never interested in it.
 * 
 * @author René M. de Bloois
 */
public class PushbackReader
{
	/**
	 * The underlying reader.
	 */
	protected Reader reader;

	/**
	 * The push back buffer.
	 */
	protected StringBuilder buffer = new StringBuilder();

	/**
	 * The mark buffer.
	 */
	protected StringBuilder mark;

	/**
	 * The mark limit.
	 */
	protected int markLimit;

	/**
	 * The current line number.
	 */
	protected int lineNumber;


	/**
	 * Constructs a new instance of the PushbackReader.
	 * 
	 * @param reader The underlying reader.
	 * @param currentLineNumber The current line number in the reader.
	 */
	public PushbackReader( Reader reader, int currentLineNumber )
	{
		this.reader = reader;
		this.lineNumber = currentLineNumber;
	}

	/**
	 * Returns the current line number.
	 * 
	 * @return The current line number.
	 */
	public int getLineNumber()
	{
		return this.lineNumber;
	}

	/**
	 * Returns the underlying reader. But only if the back buffer is empty, otherwise an IllegalStateException is thrown.
	 * 
	 * @return The underlying reader.
	 */
	public Reader getReader()
	{
		if( this.buffer.length() > 0 )
			throw new IllegalStateException( "There are still pushed back characters in the buffer" );
		return this.reader;
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
			result = this.buffer.charAt( p ); // No \r in the buffer
			this.buffer.delete( p, p + 1 ); // No cost involved, deleting from the end only decrements a count
		}
		else
		{
			try
			{
				result = this.reader.read();
				if( result == '\r' ) // Filter out carriage returns
				{
					int ch = this.reader.read();
					if( ch != '\n' )
						push1( ch );
					result = '\n';
				}
			}
			catch( IOException e )
			{
				throw new SystemException( e );
			}
		}

		if( result == '\n' )
			this.lineNumber++;

		if( this.mark != null ) // Mark enabled?
		{
			if( this.mark.length() >= this.markLimit )
				this.mark = null; // The mark is expired
			else
				this.mark.append( (char)result );
		}

		return result;
	}

	private void push1( int ch )
	{
		if( ch == '\r' )
			throw new IllegalArgumentException( "A \\r can't be pushed back into the reader" );
		if( ch != -1 )
		{
			if( ch == '\n' )
				this.lineNumber--;
			this.buffer.append( (char)ch );
		}
	}

	/**
	 * Push a character back into the reader. The current line number is decremented when a newline character is pushed back. If a mark is present it will expire.
	 * 
	 * @param ch The character to push back. -1 is ignored.
	 */
	public void push( int ch )
	{
		this.mark = null;
		push1( ch );
	}

	/**
	 * Push a complete {@link StringBuilder} back into the reader. The current line number is decremented for each newline encountered. If a mark is present it will expire.
	 * 
	 * @param builder The {@link StringBuilder} to push back.
	 */
	public void push( StringBuilder builder )
	{
		this.mark = null;
		int len = builder.length();
		while( len > 0 )
			push1( builder.charAt( --len ) ); // Use push to decrement the line number when a \n is found
	}

	/**
	 * Push a complete {@link String} back into the reader. The current line number is decremented for each newline encountered. If a mark is present it will expire.
	 * 
	 * @param string The {@link String} to push back.
	 */
	public void push( String string )
	{
		this.mark = null;
		int len = string.length();
		while( len > 0 )
			push1( string.charAt( --len ) ); // Use push to decrement the line number when a \n is found
	}

	/**
	 * Marks the current position in the reader. The mark will expire whenever one of the push methods is called or when
	 * more characters are being read than the limit allows.
	 * 
	 * @param limit When more characters are being read than this limit signifies, the mark is expired.
	 * 
	 * @see #reset()
	 */
	public void mark( int limit )
	{
		if( limit <= 0 )
			throw new IllegalArgumentException( "limit must be greater than 0" );
		this.mark = new StringBuilder();
		this.markLimit = limit;
	}

	/**
	 * Resets the reader to the marked position.
	 * 
	 * @see #mark(int)
	 */
	public void reset()
	{
		if( this.mark == null )
			throw new IllegalStateException( "No mark or mark expiried" );
		push( this.mark );
	}
}
