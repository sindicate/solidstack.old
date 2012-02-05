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


/**
 * A ConvertingWriter that converts JavaScript specific data types to Java data types.
 * 
 * @author René de Bloois
 */
public class JavaScriptConvertingWriter implements ConvertingWriter
{
	/**
	 * The EncodingWriter to write to.
	 */
	protected EncodingWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param writer The EncodingWriter to write to.
	 */
	public JavaScriptConvertingWriter( EncodingWriter writer )
	{
		this.writer = writer;
	}

	public void write( Object o ) throws IOException
	{
		if( o == null )
			this.writer.write( null );
		else
			this.writer.write( o.toString() );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		if( o == null )
			this.writer.writeEncoded( null );
		else
		{
			if( this.writer.stringsOnly() )
				this.writer.writeEncoded( o.toString() );
			else
				this.writer.writeEncoded( o );
		}
	}
}
