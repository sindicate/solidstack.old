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

package solidstack.script.objects;

import java.util.List;


// FIXME So how do we know what was part of the string and what was a substituted value?
public class FunnyString
{
	private List<Object> values;

	public FunnyString( List<Object> values )
	{
		this.values = values;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		for( Object value : this.values )
			result.append( value );
		return result.toString();
	}

	public boolean isEmpty()
	{
		for( Object value : this.values )
			if( value.toString().length() != 0 ) // TODO What about nulls?
				return false;
		return true;
	}

	public int size()
	{
		return toString().length();
	}
}
