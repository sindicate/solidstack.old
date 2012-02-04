package solidstack.template;

import java.io.IOException;

import solidstack.query.GStringWriter;


public class JavaScriptConvertingWriter implements ConvertingWriter
{
	protected EncodingWriter writer;

	public JavaScriptConvertingWriter( EncodingWriter writer )
	{
		this.writer = writer;
	}

	public void write( Object o ) throws IOException
	{
		if( o == null )
			this.writer.write( null );
		else if( o instanceof String )
			this.writer.write( (String)o );
		else
			this.writer.write( o.toString() );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		if( this.writer.supportsValues() )
			( (GStringWriter)this.writer ).writeValue( o );
		else if( o == null )
			this.writer.writeEncoded( null );
		else if( o instanceof String )
			this.writer.writeEncoded( (String)o );
		else
			this.writer.writeEncoded( o.toString() );
	}
}
