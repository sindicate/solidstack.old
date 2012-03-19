/*--
 * Copyright 2012 Ren� M. de Bloois
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;


/**
 * A factory for source readers.
 *
 * @author Ren� M. de Bloois
 */
public class SourceReaders
{
	/**
	 * @param resource The resource.
	 * @return A source reader for the given resource.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	static public SourceReader forResource( Resource resource ) throws FileNotFoundException
	{
		return forResource( resource, null, null );
	}

	/**
	 * @param resource The resource.
	 * @param encoding The encoding to use.
	 * @return A source reader for the given resource.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	static public SourceReader forResource( Resource resource, String encoding ) throws FileNotFoundException
	{
		return forResource( resource, null, encoding );
	}

	/**
	 * @param resource The resource.
	 * @param detector The encoding detector to use.
	 * @return A source reader for the given resource.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	static public SourceReader forResource( Resource resource, EncodingDetector detector ) throws FileNotFoundException
	{
		return forResource( resource, detector, null );
	}

	/**
	 * @param resource The resource.
	 * @param detector The encoding detector to use.
	 * @param defaultEncoding The encoding to use when the detector does not find an encoding.
	 * @return A source reader for the given resource.
	 * @throws FileNotFoundException When the resource is not found.
	 */
	static public SourceReader forResource( Resource resource, EncodingDetector detector, String defaultEncoding ) throws FileNotFoundException
	{
		InputStream is = new BufferedInputStream( resource.newInputStream() );
		boolean success = false;
		try
		{
			if( detector != null )
			{
				is.mark( 256 );

				byte[] buffer = new byte[ 256 ]; // Initialized with zeros by the JVM
				int len = is.read( buffer );

				is.reset();

				if( len < 256 )
				{
					byte[] bytes = new byte[ len ];
					System.arraycopy( buffer, 0, bytes, 0, len );
					buffer = bytes;
				}

				String encoding = detector.detect( buffer );
				if( encoding != null )
					defaultEncoding = encoding;
			}

			if( defaultEncoding == null )
				defaultEncoding = Charset.defaultCharset().name();

			Reader reader = new InputStreamReader( is, defaultEncoding );

			success = true;
			return new ReaderSourceReader( reader, resource.getLocation(), defaultEncoding );
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
	 * @param text The text.
	 * @param location The location of the text.
	 * @return A source reader for the given text.
	 */
	static public SourceReader forString( String text, SourceLocation location )
	{
		return new ReaderSourceReader( new StringReader( text ), location );
	}

	/**
	 * @param text The text.
	 * @return A source reader for the given text.
	 */
	static public SourceReader forString( String text )
	{
		return new ReaderSourceReader( new StringReader( text ) );
	}
}
