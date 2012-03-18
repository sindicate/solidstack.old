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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;


/**
 * Wraps a {@link Reader} and adds a line counting functionality.
 *
 * @author René M. de Bloois
 */
public class SourceReaders
{
	static public SourceReader forResource( Resource resource ) throws FileNotFoundException
	{
		return forResource( resource, null, null );
	}

	static public SourceReader forResource( Resource resource, String encoding ) throws FileNotFoundException
	{
		return forResource( resource, null, encoding );
	}

	static public SourceReader forResource( Resource resource, EncodingDetector detector ) throws FileNotFoundException
	{
		return forResource( resource, detector, null );
	}

	static public SourceReader forResource( Resource resource, EncodingDetector detector, String defaultEncoding ) throws FileNotFoundException
	{
		InputStream is = new BufferedInputStream( resource.getInputStream() );
		boolean success = false;
		try
		{
			if( detector != null )
			{
				is.mark( 256 );

				byte[] buffer = new byte[ 256 ]; // Initialized with zeros by the JVM
				int len = is.read( buffer );

				is.reset();

				String encoding = detector.detect( buffer, len );
				if( encoding != null )
					defaultEncoding = encoding;
			}

			if( defaultEncoding == null )
				defaultEncoding = Charset.defaultCharset().name();

			// TODO Do we need this BufferedReader?
			Reader reader = new BufferedReader( new InputStreamReader( is, defaultEncoding ) );

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


	static public SourceReader forString( String text, SourceLocation location )
	{
		return new ReaderSourceReader( new StringReader( text ), location );
	}

	static public SourceReader forString( String text )
	{
		return new ReaderSourceReader( new StringReader( text ) );
	}
}
