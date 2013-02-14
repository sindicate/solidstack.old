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

package solidstack.script.objects;



public class PString
{
	private String[] fragments;
	private Object[] values;

	public PString( String[] fragments, Object[] values )
	{
		this.fragments = fragments;
		this.values = values;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		int len = this.fragments.length;
		int v = 0;
		for( int i = 0; i < len; i++ )
		{
			if( this.fragments[ i ] != null )
				result.append( this.fragments[ i ] );
			else
				result.append( this.values[ v++ ] );
		}
		return result.toString();
	}

	public boolean isEmpty()
	{
		return toString().length() == 0;
	}

	public int size()
	{
		return toString().length();
	}

	public Object[] getValues()
	{
		return this.values;
	}
}
