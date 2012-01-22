package solidstack.template;

import java.io.IOException;
import java.io.Writer;

public class TemplateWriter
{
	protected Writer writer;

	public TemplateWriter( Writer writer )
	{
		this.writer = writer;
	}

	public void write( String s ) throws IOException
	{
		this.writer.write( s );
	}

	public void writeEscaped( String s ) throws IOException
	{
		this.writer.write( s );
	}
}
