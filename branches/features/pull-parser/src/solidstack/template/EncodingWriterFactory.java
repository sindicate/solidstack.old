package solidstack.template;

import java.io.Writer;


/**
 * Creates a new {@link EncodingWriter}.
 * 
 * @author René de Bloois
 */
public interface EncodingWriterFactory
{
	/**
	 * Creates a new {@link EncodingWriter}.
	 * 
	 * @param writer The writer that the EncodingWriter should write to.
	 * @return The EncodingWriter.
	 */
	EncodingWriter createWriter( Writer writer );
}
