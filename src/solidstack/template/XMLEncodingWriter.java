/*--
 * Copyright 2012 Ren� M. de Bloois
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
 * @author Ren� M. de Bloois
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
	 * Write the specified value to the writer XML encoded. &, <, >, " and ' are encoded to &amp;, &lt;, &gt;, &#034; and &#039; respectively.
	 *
	 * @param value The value to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	@Override
	public void writeEncoded( Object value ) throws IOException
	{
		if( value == null )
			return;

		char[] chars = ( (String)value ).toCharArray();
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
				this.out.write( chars, start, i - start ); // FIXME Should call the write() in the super
				this.out.write( replace );
				replace = null;
				start = ++i;
			}
			else
				i++;
		}
		this.out.write( chars, start, len - start );
	}
}
