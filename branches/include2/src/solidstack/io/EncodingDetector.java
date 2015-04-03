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

package solidstack.io;

/**
 * Detects the character encoding from the first line.
 *
 * @author René de Bloois
 */
public interface EncodingDetector
{
	/**
	 * Constant for the ISO-8859-1 character set.
	 */
	static final public String CHARSET_ISO_8859_1 = "ISO-8859-1";

	/**
	 * Constant for the UTF-8 character set.
	 */
	static final public String CHARSET_UTF_8 = "UTF-8";

	/**
	 * Constant for the UTF-16 character set.
	 */
	static final public String CHARSET_UTF_16 = "UTF-16";

	/**
	 * Constant for the UTF-16BE character set.
	 */
	static final public String CHARSET_UTF_16BE = "UTF-16BE";

	/**
	 * Constant for the UTF-16LE character set.
	 */
	static final public String CHARSET_UTF_16LE = "UTF-16LE";

	/**
	 * Constant for the UTF-32 character set.
	 */
	static final public String CHARSET_UTF_32 = "UTF-32";

	/**
	 * Constant for the UTF-32BE character set.
	 */
	static final public String CHARSET_UTF_32BE = "UTF-32BE";

	/**
	 * Constant for the UTF-32LE character set.
	 */
	static final public String CHARSET_UTF_32LE = "UTF-32LE";

	/**
	 * @param bytes The first couple of bytes from the source.
	 * @return The detected character encoding.
	 */
	String detect( byte[] bytes );
}
