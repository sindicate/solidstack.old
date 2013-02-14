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

package solidstack.template;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.Resource;
import solidstack.io.Resources;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;


@SuppressWarnings( "javadoc" )
public class EncodingTests
{
	static public void main( String[] args ) throws IOException
	{
		Map< String, Charset > sets = Charset.availableCharsets();
		for( String name : sets.keySet() )
			System.out.println( name );
	}

	static private void _test1( byte[] bytes, String encoding ) throws FileNotFoundException
	{
		Resource resource = Resources.getResource( bytes );
		SourceReader reader = SourceReaders.forResource( resource, EncodingDetector.INSTANCE );
		Assert.assertEquals( reader.getEncoding(), encoding );
	}

	static private void _test2( String encoding ) throws FileNotFoundException, UnsupportedEncodingException
	{
		String in = "<%@ template encoding=\"" + EncodingDetector.CHARSET_ISO_8859_1 + "\" %>";
		byte[] bytes = in.getBytes( encoding );
		Resource resource = Resources.getResource( bytes );
		SourceReader reader = SourceReaders.forResource( resource, EncodingDetector.INSTANCE );
		Assert.assertEquals( reader.getEncoding(), EncodingDetector.CHARSET_ISO_8859_1 );
	}

	@Test
	static public void test1() throws UnsupportedEncodingException, FileNotFoundException
	{
		_test1( new byte[] { 32, 32 }, EncodingDetector.CHARSET_UTF_8 );
		_test1( new byte[] { 32, 0 }, EncodingDetector.CHARSET_UTF_16LE );
		_test1( new byte[] { 0, 32 }, EncodingDetector.CHARSET_UTF_16BE );
		_test1( new byte[] { 32, 0, 32, 0 }, EncodingDetector.CHARSET_UTF_16LE );
		_test1( new byte[] { 0, 32, 0, 32 }, EncodingDetector.CHARSET_UTF_16BE );
		_test1( new byte[] { 32, 0, 0, 0 }, EncodingDetector.CHARSET_UTF_32LE );
		_test1( new byte[] { 0, 0, 0, 32 }, EncodingDetector.CHARSET_UTF_32BE );

		_test2( EncodingDetector.CHARSET_ISO_8859_1 );
		_test2( EncodingDetector.CHARSET_UTF_8 );
		_test2( EncodingDetector.CHARSET_UTF_16 );
		_test2( EncodingDetector.CHARSET_UTF_16LE );
		_test2( EncodingDetector.CHARSET_UTF_16BE );
		_test2( EncodingDetector.CHARSET_UTF_32 );
		_test2( EncodingDetector.CHARSET_UTF_32LE );
		_test2( EncodingDetector.CHARSET_UTF_32BE );

		/*
		 * Encoding detection does not work with: IBM-Thai, IBM01140, IBM01141, IBM01142, IBM01143, IBM01144, IBM01145,
		 * IBM01146, IBM01147, IBM01148, IBM01149, IBM037, IBM1026, IBM1047, IBM273, IBM277, IBM278, IBM280, IBM284,
		 * IBM285, IBM297, IBM420, IBM424, IBM500, IBM864, IBM870, IBM871, IBM918, ISO-2022-CN, JIS_X0212-1990,
		 * x-IBM1025, x-IBM1097, x-IBM1112, x-IBM1122, x-IBM1123, x-IBM1364, x-IBM833, x-IBM834, x-IBM875, x-IBM930,
		 * x-IBM933, x-IBM935, x-IBM937, x-IBM939, x-JIS0208, x-JISAutoDetect, x-MacDingbat, x-MacSymbol
		 */
		/*
		for( String charset : Charset.availableCharsets().keySet() )
		{
			try
			{
				_test2( charset );
				System.out.println( "Succeeded: " + charset );
			}
			catch( Error e )
			{
				System.out.println( charset + ": error: " + e.getMessage() );
			}
			catch( Exception e )
			{
				System.out.println( charset + ": exception: " + e.getMessage() );
			}
		}
		*/
	}

	@Test
	static public void test2() throws UnsupportedEncodingException, FileNotFoundException
	{
		String s = "<%@ template encoding=\"" + EncodingDetector.CHARSET_ISO_8859_1 + "\" version=\"1.0\" %>";
		byte[] bytes = s.getBytes( EncodingDetector.CHARSET_UTF_16 );
		Resource resource = Resources.getResource( bytes );
		SourceReader reader = SourceReaders.forResource( resource, EncodingDetector.INSTANCE );
		Assert.assertEquals( reader.getEncoding(), EncodingDetector.CHARSET_ISO_8859_1 );

		s = "<%@ template version=\"1.0\" encoding=\"" + EncodingDetector.CHARSET_ISO_8859_1 + "\" %>";
		bytes = s.getBytes( EncodingDetector.CHARSET_UTF_16 );
		resource = Resources.getResource( bytes );
		reader = SourceReaders.forResource( resource, EncodingDetector.INSTANCE );
		Assert.assertEquals( reader.getEncoding(), EncodingDetector.CHARSET_ISO_8859_1 );
	}

	@Test
	static public void test3() throws UnsupportedEncodingException, FileNotFoundException
	{
		String text = "\u00EF\u00BB\u00BF<%@ template encoding=\"" + EncodingDetector.CHARSET_UTF_8 + "\" %>";
		byte[] bytes = text.getBytes( "ISO-8859-1" );
		Assert.assertEquals( bytes[ 0 ], -17 );
		Assert.assertEquals( bytes[ 1 ], -69 );
		Assert.assertEquals( bytes[ 2 ], -65 );

		Resource resource = Resources.getResource( bytes );
		SourceReader reader = SourceReaders.forResource( resource, EncodingDetector.INSTANCE );
		Assert.assertEquals( reader.getEncoding(), EncodingDetector.CHARSET_UTF_8 );
		Assert.assertEquals( reader.read(), '<' );
	}

	@Test
	static public void test4() throws IOException
	{
		skipTest( "UTF-8", 7, false, false );
		skipTest( "UTF-16", 10, true, true ); // Adds, skips
		skipTest( "UTF-16BE", 10, false, false );
		skipTest( "UTF-16LE", 10, false, false );
		skipTest( "UTF-32", 20, false, true ); // Skips
		skipTest( "UTF-32BE", 20, false, true ); // Skips
		skipTest( "UTF-32LE", 20, false, true ); // Skips
		skipTest( "X-UTF-16LE-BOM", 10, true, true ); // Adds, skips
		skipTest( "X-UTF-32BE-BOM", 20, true, true ); // Adds, skips
		skipTest( "X-UTF-32LE-BOM", 20, true, true ); // Adds, skips
	}

	static private void skipTest( String charset, int byteCount, boolean javaAdds, boolean skips ) throws IOException
	{
		String text = javaAdds ? "TEST" : "\uFEFFTEST";
		byte[] bytes = text.getBytes( charset );
		Assert.assertEquals( bytes.length, byteCount );
		ByteArrayInputStream in = new ByteArrayInputStream( bytes );
		Reader reader = new InputStreamReader( in, charset );
		int ch = reader.read();
		Assert.assertEquals( ch != 0xFEFF, skips, ch != 0xFEFF ? charset + " skips the BOM" : charset + " does not skip the BOM" );
	}
}
