package solidstack.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceLineReader extends ReaderLineReader
{
	private String encoding;

	public ResourceLineReader( Resource resource ) throws FileNotFoundException
	{
		this( resource, null, null );
	}

	public ResourceLineReader( Resource resource, String encoding ) throws FileNotFoundException
	{
		this( resource, null, encoding );
	}

	public ResourceLineReader( Resource resource, EncodingDetector detector ) throws FileNotFoundException
	{
		this( resource, detector, null );
	}

	/**
	 * @param resource The resource to read from.
	 * @param encodingDetection A regular expression to detect the encoding on the first line.
	 * @throws FileNotFoundException When a file is not found.
	 */
	private ResourceLineReader( Resource resource, EncodingDetector detector, String defaultEncoding ) throws FileNotFoundException
	{
		this.resource = resource;
		this.encoding = defaultEncoding;

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
					this.encoding = encoding;
			}

			if( this.encoding == null )
				this.encoding = "ISO-8859-1"; // TODO

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
	 * Returns the current character encoding of the stream.
	 *
	 * @return The current character encoding of the stream.
	 */
	@Override
	public String getEncoding()
	{
		return this.encoding;
	}
}
