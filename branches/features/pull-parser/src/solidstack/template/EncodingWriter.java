package solidstack.template;

import java.io.IOException;
import java.io.Writer;

/**
 * An encoding writer. Adds a {@link #writeEncoded(String)} method. This implementation does not encode.
 * 
 * @author René M. de Bloois
 *
 */
public class EncodingWriter extends Writer
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
	public EncodingWriter( Writer writer )
	{
		this.writer = writer;
	}

	@Override
	public void write( char buf[], int off, int len ) throws IOException
	{
		this.writer.write( buf, off, len );
	}

	/**
	 * Write the specified string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	public void writeEncoded( String s ) throws IOException
	{
		write( s );
	}

	@Override
	public void flush() throws IOException
	{
		this.writer.flush();
	}

	@Override
	public void close() throws IOException
	{
		this.writer.close();
	}
}
