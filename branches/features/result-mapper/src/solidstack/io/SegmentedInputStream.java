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
import java.io.InputStream;


/**
 * Divides an input stream into multiple segments.
 */
public class SegmentedInputStream extends InputStream
{
	private InputStream in;
	private long index; // The current index
	private long segmentEnd; // Where the current segment ends

	/**
	 * @param in The input stream that must be divided into segments.
	 */
	public SegmentedInputStream( InputStream in )
	{
		this.in = in;
	}

	@Override
	public int read() throws IOException
	{
		if( this.index >= this.segmentEnd )
			return -1;
		int result = this.in.read();
		if( result == -1 )
			throw new FatalIOException( "Segment not complete" );
		this.index ++;
		return result;
	}

	@Override
	public int read( byte[] b, int off, int len ) throws IOException
	{
//		System.out.println( "Read " + this.index );
		if( this.index >= this.segmentEnd )
			return -1;
		int read;
		if( len <= this.segmentEnd - this.index )
			read = this.in.read( b, off, len );
		else
			read = this.in.read( b, off, (int)( this.segmentEnd - this.index ) );
		if( read == -1 )
			throw new FatalIOException( "Segment not complete" );
		this.index += read;
		return read;
	}

	@Override
	public void close() throws IOException
	{
		this.in.close();
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
		this.in.skip( start - this.index );
		this.index = start;
		this.segmentEnd = start + length;
	}

	/**
	 * Returns an input stream that reads a segment from the larger input stream.
	 *
	 * @param start The start index of the segment.
	 * @param length The length of the segment.
	 * @return An input stream that reads a segment from the larger input stream.
	 */
	public InputStream getSegmentInputStream( long start, long length )
	{
		return new SegmentInputStream( start, length );
	}

	/**
	 * An input stream that reads a segment from a larger input stream.
	 */
	public class SegmentInputStream extends InputStream
	{
		private long start;
		private long length;
		private boolean accessed;

		/**
		 * @param start The start of the segment.
		 * @param length The length of the segment.
		 */
		public SegmentInputStream( long start, long length )
		{
			this.start = start;
			this.length = length;
		}

		@Override
		public int read() throws IOException
		{
			if( !this.accessed )
			{
				skipToSegment( this.start, this.length );
				this.accessed = true;
			}
			return SegmentedInputStream.this.read();
		}

		@Override
		public int read( byte[] b, int off, int len ) throws IOException
		{
			if( !this.accessed )
			{
				skipToSegment( this.start, this.length );
				this.accessed = true;
			}
			return SegmentedInputStream.this.read( b, off, len );
		}

		@Override
		public void close() throws IOException
		{
			// No closing
		}
	}
}
