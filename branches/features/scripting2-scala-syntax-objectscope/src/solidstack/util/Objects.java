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
 * Support for objects.
 *
 * @author René de Bloois
 */
public class Objects
{
	/**
	 * @param o1 The first object.
	 * @param o2 The second object.
	 * @return True if both objects are null or equal, false otherwise.
	 */
	static public boolean equals( Object o1, Object o2 )
	{
		if( o1 == o2 )
			return true;
		if( o1 == null )
			return false;
		return o1.equals( o2 );
	}
}
