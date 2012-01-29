/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.template;

import java.io.IOException;
import java.io.Writer;


/**
 * An encoding writer for XML.
 * 
 * @author René M. de Bloois
 */
public class XMLEncodingWriter extends NoEncodingWriter
{
	/**
	 * A factory for producing new XMLEncodingWriters.
	 */
	static public final EncodingWriterFactory FACTORY = new EncodingWriterFactory()
	{
		//@Override
		public NoEncodingWriter createWriter( Writer writer )
		{
			return new XMLEncodingWriter( writer );
		}
	};

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
		if( s == null )
			return;

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
				this.writer.write( chars, start, i - start );
				this.writer.write( replace );
				replace = null;
				start = ++i;
			}
			else
				i++;
		}
		this.writer.write( chars, start, len - start );
	}
}
