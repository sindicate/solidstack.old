package solidstack.template;

import java.io.Writer;

public interface EncodingWriterFactory
{
	EncodingWriter createWriter( Writer writer );
}
