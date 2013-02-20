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
import java.io.Reader;


/**
 * Divides an reader into multiple segments.
 */
public class SegmentedReader extends Reader
{
	private Reader reader;
	private long index;
	private long segmentEnd;

	/**
	 * @param reader The reader that must be divided into segments.
	 */
	public SegmentedReader( Reader reader )
	{
		super( reader );
		this.reader = reader;
	}

	@Override
	public int read( char[] cbuf, int off, int len ) throws IOException
	{
		if( this.index >= this.segmentEnd )
			return -1;
		int read;
		if( len <= this.segmentEnd - this.index )
			read = this.reader.read( cbuf, off, len );
		else
			read = this.reader.read( cbuf, off, (int)( this.segmentEnd - this.index ) );
		if( read == -1 )
			throw new FatalIOException( "Segment not complete" );
		this.index += read;
		return read;
	}

	@Override
	public void close() throws IOException
	{
		this.reader.close();
	}

	/**
	 * Skip to a segment.
	 *
	 * @param start The start index of the segment.
	 * @param length The length of the segment.
	 * @throws IOException Whenever the input stream throws an exception.
	 */
	public void skipToSegment( long start, long length ) throws IOException
	{
		if( this.index > start )
			throw new IOException( "Past segment" );
		this.reader.skip( start - this.index );
		this.index = start;
		this.segmentEnd = start + length;
	}

	/**
	 * Returns reader that reads a segment from the larger reader.
	 *
	 * @param start The start index of the segment.
	 * @param length The length of the segment.
	 * @return A reader that reads a segment from the larger reader.
	 */
	public Reader getSegmentReader( long start, long length )
	{
		return new SegmentReader( start, length );
	}

	/**
	 * A reader that reads a segment from a larger reader.
	 */
	public class SegmentReader extends Reader
	{
		private long start;
		private long length;
		private boolean accessed;

		/**
		 * @param start The start of the segment.
		 * @param length The length of the segment.
		 */
		public SegmentReader( long start, long length )
		{
			this.start = start;
			this.length = length;
		}

		@Override
		public int read( char[] cbuf, int off, int len ) throws IOException
		{
			if( !this.accessed )
			{
				skipToSegment( this.start, this.length );
				this.accessed = true;
			}
			return SegmentedReader.this.read( cbuf, off, len );
		}

		@Override
		public void close() throws IOException
		{
			// No closing
		}
	}
}
