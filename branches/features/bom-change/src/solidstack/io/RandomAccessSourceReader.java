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
 * A line reader that automatically detects character encoding through the BOM and is able to reposition itself on a line.
 *
 * @author René M. de Bloois
 */
public class RandomAccessSourceReader implements SourceReader
{
	private Resource resource;
	private SourceReader reader;
	private String encodingOverride;
	private EncodingDetector detector;

	public RandomAccessSourceReader( Resource resource ) throws FileNotFoundException
	{
		// TODO Check that the resource is reopenable
		this.resource = resource;
		this.reader = ReaderSourceReader.forResource( resource );
	}

	public RandomAccessSourceReader( Resource resource, EncodingDetector detector ) throws FileNotFoundException
	{
		// TODO Check that the resource is reopenable
		this.resource = resource;
		this.detector = detector;
		this.reader = ReaderSourceReader.forResource( resource, detector );
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

	public void gotoLine( int lineNumber )
	{
		if( this.reader == null )
			throw new IllegalStateException( "Stream is not open" ); // TODO Is this what the JDK throws too?
		if( lineNumber < 1 )
			throw new IllegalArgumentException( "lineNumber must be 1 or greater" );

		if( lineNumber < getLineNumber() )
			reOpen();
		while( lineNumber > getLineNumber() )
			if( readLine() == null )
				throw new IllegalArgumentException( "lineNumber " + lineNumber + " not found" );
	}

	public void close()
	{
		if( this.reader != null )
			this.reader.close();
		this.reader = null;
	}

	/**
	 * Reopens itself to reset the position or change the character encoding.
	 */
	protected void reOpen()
	{
		close();
		try
		{
			this.reader = ReaderSourceReader.forResource( this.resource, this.detector );
		}
		catch( FileNotFoundException e )
		{
			throw new FatalIOException( e );
		}
	}
}
