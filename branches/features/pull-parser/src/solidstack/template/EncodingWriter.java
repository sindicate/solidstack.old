package solidstack.template;

import java.io.IOException;

/**
 * An encoding writer. Adds a {@link #writeEncoded(String)} method. This implementation does not encode.
 * 
 * @author René M. de Bloois
 *
 */
public interface EncodingWriter
{
	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void write( String s ) throws IOException;

	/**
	 * Write the specified string to the writer encoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void writeEncoded( String s ) throws IOException;
}
