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

import java.io.BufferedReader;
import java.io.IOException;

import solidstack.SystemException;


/**
 * Wraps a {@link BufferedReader} and adds a line counting functionality.
 *
 * @author René M. de Bloois
 */
// TODO Copy all these util class back to solidbase
public class BufferedReaderLineReader implements LineReader
{
	/**
	 * The reader used to read from the string.
	 */
	protected BufferedReader reader;

	/**
	 * The current line the reader is positioned on.
	 */
	protected int currentLineNumber;

	protected int keep = -1;

	/**
	 * The current position in the {@link #buffer}.
	 */
	protected int pos;

	/**
	 * The underlying resource.
	 */
	protected Resource resource;


	/**
	 * Close the reader and the underlying reader.
	 */
	public void close()
	{
		if( this.reader != null )
		{
			try
			{
				this.reader.close();
			}
			catch( IOException e )
			{
				throw new SystemException( e );
			}
			this.reader = null;
		}
	}

	public String readLine()
	{
		StringBuilder result = new StringBuilder();
		int ch = read();
		while( ch != '\n' && ch != -1 )
		{
			result.append( (char)ch );
			ch = read();
		}
		return result.toString();
	}

	public int getLineNumber()
	{
		if( this.reader == null )
			throw new IllegalStateException( "Closed" );
		return this.currentLineNumber;
	}

	public int read()
	{
		int result;

		try
		{
			if( this.keep != -1 )
			{
				result = this.keep;
				this.keep = -1;
			}
			else
			{
				result = this.reader.read();
			}

			if( result == '\r' ) // Filter out carriage returns
			{
				result = this.reader.read();
				if( result != '\n' )
					this.keep = result;
				result = '\n';
				this.currentLineNumber++;
			}
			else if( result == '\n' )
				this.currentLineNumber++;
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}

		return result;
	}

	public Resource getResource()
	{
		return this.resource;
	}

	public String getEncoding()
	{
		return "internal";
	}

	public byte[] getBOM()
	{
		return null;
	}

	public FileLocation getLocation()
	{
		return new FileLocation( this.resource, getLineNumber() );
	}
}
