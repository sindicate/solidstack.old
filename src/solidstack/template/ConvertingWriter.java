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
 * Accepts writes from the template. Has the responsibility to convert script language specific data types to java types
 * and pass them on to the {@link EncodingWriter}.
 * 
 * @author René de Bloois
 */
public interface ConvertingWriter
{
	/**
	 * Write the object to the writer.
	 * 
	 * @param o The object to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void write( Object o ) throws IOException;

	/**
	 * Writes an object from a ${ expression to the writer.
	 * 
	 * @param o The object to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void writeEncoded( Object o ) throws IOException;
}
