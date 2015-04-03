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
 * An encoding writer. Can encode strings to the target content type.
 * 
 * @author René M. de Bloois
 */
public interface EncodingWriter
{
	/**
	 * Write the string to the writer unencoded.
	 * 
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void write( String s ) throws IOException;

	/**
	 * Write the value to the writer encoded.
	 * 
	 * @param value The value to write.
	 * @throws IOException Whenever an IOException occurs.
	 */
	void writeEncoded( Object value ) throws IOException;

	/**
	 * Indicates that {@link #writeEncoded(Object)} only accepts Strings, not Objects.
	 * 
	 * @return True when {@link #writeEncoded(Object)} only accepts Strings, not Objects.
	 */
	boolean stringsOnly();
}
