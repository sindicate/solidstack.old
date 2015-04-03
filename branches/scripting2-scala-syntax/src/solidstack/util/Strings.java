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

package solidstack.util;


/**
 * Support for strings.
 *
 * @author René de Bloois
 */
public class Strings extends Objects
{
	/**
	 * @param s1 The first string.
	 * @param s2 The second string.
	 * @return True if both strings are null or equal ignoring the case, false otherwise.
	 */
	static public boolean equalsIgnoreCase( String s1, String s2 )
	{
		if( s1 == s2 )
			return true;
		if( s1 == null )
			return false;
		return s1.equalsIgnoreCase( s2 );
	}
}
