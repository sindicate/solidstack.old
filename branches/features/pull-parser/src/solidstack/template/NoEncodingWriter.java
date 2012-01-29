package solidstack.template;

import java.io.IOException;
import java.io.Writer;

/**
 * An encoding writer. Adds a {@link #writeEncoded(String)} method. This implementation does not encode.
 * 
 * @author René M. de Bloois
 *
 */
// Can't implement Writer. DefaultGroovyMethods.write(Writer self, Writable writable) will be called when value is null, which results in NPE.
public class NoEncodingWriter implements EncodingWriter
{
	/**
	 * The writer to write to.
	 */
	protected Writer writer;

	/**
	 * Constructor.
	 * 
	 * @param writer The writer to write to.
	 */
	public NoEncodingWriter( Writer writer )
	{
		this.writer = writer;
	}

	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void write( String s ) throws IOException
	{
		if( s == null )
			return;

		this.writer.write( s );
	}

	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void writeEncoded( String s ) throws IOException
	{
		if( s == null )
			return;

		write( s );
	}
}
