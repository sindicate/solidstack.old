package solidstack.template;

import java.io.IOException;

public interface ConvertingWriter
{
	/**
	 * Write the object to the writer unencoded.
	 * 
	 * @param o The object to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void write( Object o ) throws IOException;

	/**
	 * Write the object to the writer encoded.
	 * 
	 * @param o The object to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void writeEncoded( Object o ) throws IOException;
}
