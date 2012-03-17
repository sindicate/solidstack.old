package solidstack.template;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import solidstack.lang.SystemException;

public class EncodingDetector implements solidstack.io.EncodingDetector
{
	static final private Pattern ENCODING_PATTERN = Pattern.compile( "^<%@[ \t]*template[ \t]+encoding[ \t]*=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE ); // TODO Improve, case sensitive?

	/**
	 * Constant for the ISO-8859-1 character set.
	 */
	static final public String CHARSET_ISO = "ISO-8859-1";

	/**
	 * Constant for the UTF-8 character set.
	 */
	static final public String CHARSET_UTF8 = "UTF-8";

	/**
	 * Constant for the UTF character set.
	 */
	static final public String CHARSET_UTF = "UTF";

	/**
	 * Constant for the UTF-16BE character set.
	 */
	static final public String CHARSET_UTF16BE = "UTF-16BE";

	/**
	 * Constant for the UTF-16LE character set.
	 */
	static final public String CHARSET_UTF16LE = "UTF-16LE";

	static final public String CHARSET_UTF32BE = "UTF-32BE";

	static final public String CHARSET_UTF32LE = "UTF-32LE";

	static final public EncodingDetector INSTANCE = new EncodingDetector();


	private EncodingDetector()
	{
		// Singleton
	}

	public String detect( byte[] bytes, int len )
	{
		String result = CHARSET_UTF; // Default

		String first = toAscii( bytes, len );
		Matcher matcher = ENCODING_PATTERN.matcher( first );
		if( matcher.matches() )
			result = matcher.group( 1 );

		// TODO When UTF-8, test the the JVM skips the optional byte order mark. Also for UTF-16BE/LE.

		if( !CHARSET_UTF.equals( result ) )
			return result;

		return detectUTF( bytes, len );
	}

	// Only works when first 2 characters are ascii
	static private String detectUTF( byte[] bytes, int len )
	{
		// xx xx xx xx  UTF-8
		// xx 00 xx 00  UTF-16LE
		// 00 xx 00 xx  UTF-16BE (default for UTF-16)
		// xx 00 00 00  UTF-32LE
		// 00 00 00 xx  UTF-32BE

		// BOM is only read by the JVM when UTF-8, UTF-16 or UTF-32, or is it?
		// specifying BE or LE means the BOM is not removed when there is one, or is it not?
		// The BOM is a Zero-width non-breaking space (ZWNBSP), but is deprecated as a character, so that it can be used as a BOM
		// TODO What do the tests say? Maybe we should use UTF-16 and UTF-32 to make sure that any BOM is removed from the file.

		if( bytes[ 0 ] != 0 )
		{
			if( bytes[ 1 ] != 0 )
				return CHARSET_UTF8;
			if( bytes[ 2 ] != 0 )
				return CHARSET_UTF16LE;
			return CHARSET_UTF32LE;
		}
		if( bytes[ 1 ] != 0 )
			return CHARSET_UTF16BE;
		return CHARSET_UTF32BE;
	}

	static private String toAscii( byte[] chars, int len )
	{
		int j = 0;
		byte[] result = new byte[ len ];
		for( int i = 0; i < len; i++ )
		{
			byte ch = chars[ i ];
			if( ch > 0 && ch < 128 )
				result[ j++ ] = ch;
		}
		try
		{
			return new String( result, 0, j, "ISO-8859-1" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new SystemException( e );
		}
	}
}
