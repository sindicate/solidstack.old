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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A line reader that automatically detects character encoding through the BOM and the first line.
 *
 * @author René M. de Bloois
 */
public class CharsetDetectingLineReader extends ReaderLineReader
{
	/**
	 * Constant for the ISO-8859-1 character set.
	 */
	static final public String CHARSET_ISO = "ISO-8859-1";

	/**
	 * Constant for the UTF-8 character set.
	 */
	static final public String CHARSET_UTF8 = "UTF-8";

	/**
	 * Constant for the UTF-16BE character set.
	 */
	static final public String CHARSET_UTF16BE = "UTF-16BE";

	/**
	 * Constant for the UTF-16LE character set.
	 */
	static final public String CHARSET_UTF16LE = "UTF-16LE";

	/**
	 * Constant for the default character set, which is ISO-8859-1.
	 */
	// TODO Make this also a parameter, so that it can be overridden if not explicit in the file
	// FIXME Decide if we want UTF-8
	// TODO Change the .slt default charset, if needed
	static final public String CHARSET_DEFAULT = CHARSET_ISO;

	/**
	 * The encoding of the stream.
	 */
	protected String encoding = CHARSET_DEFAULT;

	/**
	 * The Byte Order Mark found at the beginning of the stream. Null if not present.
	 */
	protected byte[] bom;


	/**
	 * @param resource The resource to read from.
	 * @param encodingDetection A regular expression to detect the encoding on the first line.
	 * @throws FileNotFoundException When a file is not found.
	 */
	public CharsetDetectingLineReader( Resource resource, Pattern encodingDetection ) throws FileNotFoundException
	{
		this.resource = resource;

		InputStream is = new BufferedInputStream( resource.getInputStream() );
		boolean success = false;
		try
		{
			is.mark( 256 );

			byte[] buffer = new byte[ 256 ];
			int len = is.read( buffer );

			is.reset();

			String first = ascii( buffer, len );
			Matcher matcher = encodingDetection.matcher( first );
			if( matcher.matches() )
				this.encoding = matcher.group( 1 );
//			else
//				detectBOM( buffer, len ); TODO Enable this when the default is changed to UTF-8

			// TODO Also detect UTF like JSON does, and then maybe introduce generic "UTF" as charset? But should be optional, only works if first 2 characters are ascii.

			// TODO Do we need this BufferedReader?
			init( new BufferedReader( new InputStreamReader( is, this.encoding ) ) );

			success = true;
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
		finally
		{
			// When an exception occurred we need to close the input stream
			if( !success )
				try
				{
					is.close();
				}
				catch( IOException ee )
				{
					throw new FatalIOException( ee );
				}
		}
	}

	/**
	 * @param resource The resource to read from.
	 * @param encoding The encoding of the file. If not null, it will override the BOM.
	 * @throws FileNotFoundException When a file is not found.
	 */
	public CharsetDetectingLineReader( Resource resource, String encoding ) throws FileNotFoundException
	{
		this.resource = resource;

		// TODO Is a BufferedInputStream necessary?
		BufferedInputStream in = new BufferedInputStream( resource.getInputStream() );
		boolean success = false;
		try
		{
			if( encoding != null )
				this.encoding = encoding;
			init( new BufferedReader( new InputStreamReader( in, this.encoding ) ) );
			success = true;
		}
		catch( UnsupportedEncodingException e )
		{
			throw new FatalIOException( e );
		}
		finally
		{
			// When an exception occurred we need to close the input stream
			if( !success )
				try
				{
					in.close();
				}
				catch( IOException ee )
				{
					throw new FatalIOException( ee );
				}
		}
	}

	/**
	 * @param resource The resource to read from.
	 * @throws FileNotFoundException When a file is not found.
	 */
	public CharsetDetectingLineReader( Resource resource ) throws FileNotFoundException
	{
		this( resource, (String)null );
	}

	/**
	 * Detect the encoding from the BOM.
	 *
	 * @param in The input stream.
	 */
	// TODO Detect UTF32 too
	private void detectBOM( byte[] bytes, int len )
	{
		this.bom = null;

		if( len < 4 )
		{
			byte[] b = new byte[] { 2, 2, 2, 2 };
			System.arraycopy( bytes, 0, b, 0, len );
			bytes = b;
		}

		// BOMS:
		// 00 00 FE FF  UTF-32, big-endian
		// FF FE 00 00 	UTF-32, little-endian
		// FE FF 	    UTF-16, big-endian
		// FF FE 	    UTF-16, little-endian
		// EF BB BF 	UTF-8
		if( bytes[ 0 ] == -17 && bytes[ 1 ] == -69 && bytes[ 2 ] == -65 )
		{
			this.encoding = CHARSET_UTF8;
			this.bom = new byte[] { -17, -69, -65 };
		}
		else if( bytes[ 0 ] == -2 && bytes[ 1 ] == -1 )
		{
			this.encoding = CHARSET_UTF16BE;
			this.bom = new byte[] { -2, -1 };
		}
		else if( bytes[ 0 ] == -1 && bytes[ 1 ] == -2 )
		{
			this.encoding = CHARSET_UTF16LE;
			this.bom = new byte[] { -1, -2 };
		}
	}

	static private String ascii( byte[] chars, int len ) throws UnsupportedEncodingException
	{
		int j = 0;
		byte[] result = new byte[ len ];
		for( int i = 0; i < len; i++ )
		{
			byte ch = chars[ i ];
			if( ch > 0 && ch < 128 )
				result[ j++ ] = ch;
		}
		return new String( result, 0, j, "ISO-8859-1" );
	}

	/**
	 * Returns the current character encoding of the stream.
	 *
	 * @return The current character encoding of the stream.
	 */
	@Override
	public String getEncoding()
	{
		return this.encoding;
	}

	/**
	 * Returns the Byte Order Mark found in the stream.
	 *
	 * @return The Byte Order Mark found. Will be null if no BOM was present.
	 */
	@Override
	public byte[] getBOM()
	{
		return this.bom;
	}
}
