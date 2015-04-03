/*--
 * Copyright 2005 René M. de Bloois
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

import java.io.FileNotFoundException;



/**
 * A source reader that can be repositioned with {@link #gotoLine(int)}.
 *
 * @author René M. de Bloois
 */
public class RandomAccessSourceReader implements SourceReader
{
	private Resource resource;
	private SourceReader reader;
	private EncodingDetector detector;

	/**
	 * @param resource The resource that should be read.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	public RandomAccessSourceReader( Resource resource ) throws FileNotFoundException
	{
		this.resource = resource;
		this.reader = SourceReaders.forResource( resource );
	}

	/**
	 * @param resource The resource that should be read.
	 * @param detector The encoding detector to be used.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	public RandomAccessSourceReader( Resource resource, EncodingDetector detector ) throws FileNotFoundException
	{
		this.resource = resource;
		this.detector = detector;
		this.reader = SourceReaders.forResource( resource, detector );
	}

	public int read()
	{
		return this.reader.read();
	}

	public String readLine()
	{
		return this.reader.readLine();
	}

	public Resource getResource()
	{
		return this.reader.getResource();
	}

	public SourceLocation getLocation()
	{
		return this.reader.getLocation();
	}

	public int getLineNumber()
	{
		return this.reader.getLineNumber();
	}

	public String getEncoding()
	{
		return this.reader.getEncoding();
	}

	public void close()
	{
		if( this.reader != null )
			this.reader.close();
		this.reader = null;
	}

	/**
	 * Jump to the given line number in the source. If the line number is smaller than the current line number, the
	 * resource will be reopened and read from line 1.
	 *
	 * @param lineNumber The line number to jump to.
	 */
	public void gotoLine( int lineNumber )
	{
		if( this.reader == null )
			throw new FatalIOException( "Reader is closed" );
		if( lineNumber < 1 )
			throw new IllegalArgumentException( "lineNumber must be greater than zero" );

		// Re-open if needed
		if( lineNumber < getLineNumber() )
		{
			close();
			try
			{
				this.reader = SourceReaders.forResource( this.resource, this.detector );
			}
			catch( FileNotFoundException e )
			{
				throw new FatalIOException( e );
			}
		}

		// Skip lines
		while( lineNumber > getLineNumber() )
			if( readLine() == null )
				throw new IllegalArgumentException( "lineNumber " + lineNumber + " not found" );
	}
}
