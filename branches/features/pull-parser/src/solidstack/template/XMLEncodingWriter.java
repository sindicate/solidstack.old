package solidstack.template;

import java.io.IOException;
import java.io.Writer;


/**
 * An encoding writer for XML.
 * 
 * @author René M. de Bloois
 */
public class XMLEncodingWriter extends EncodingWriter
{
	static public EncodingWriterFactory getFactory()
	{
		return new EncodingWriterFactory()
		{
			//@Override
			public EncodingWriter createWriter( Writer writer )
			{
				return new XMLEncodingWriter( writer );
			}
		};
	}

	/**
	 * Constructor.
	 * 
	 * @param writer The writer to write to.
	 */
	public XMLEncodingWriter( Writer writer )
	{
		super( writer );
	}

	/**
	 * Write the specified string to the writer XML encoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	@Override
	public void writeEncoded( String s ) throws IOException
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		int start = 0;
		String replace = null;
		for( int i = 0; i < len; )
		{
			switch( chars[ i ] )
			{
				case '&': replace = "&amp;"; break;
				case '<': replace = "&lt;"; break;
				case '>': replace = "&gt;"; break;
				case '"': replace = "&#034;"; break;
				case '\'': replace = "&#039;"; break;
				default:
			}
			if( replace != null )
			{
				write( chars, start, i - start );
				write( replace );
				replace = null;
				start = ++i;
			}
			else
				i++;
		}
		write( chars, start, len - start );
	}
}
