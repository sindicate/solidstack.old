package solidstack.template;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.Resource;
import solidstack.io.Resources;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;

public class EncodingTests
{
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

	@Test(groups="new")
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

	@Test(groups="new")
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
}
