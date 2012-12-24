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

package solidstack.script.java;

import java.util.Arrays;



public class CallKey
{
	public Class type; // Type of the object, or if the object is a class, then the object itself.
	public String name; // The name of the method, null for constructor
	public boolean staticCall;
	public Class[] argTypes; // Types of the arguments.

	public CallKey( Class type, String name, boolean staticCall, Class[] argTypes )
	{
		this.type = type;
		this.name = name;
		this.staticCall = staticCall;
		this.argTypes = argTypes;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(
			new Object[] {
				this.type, this.name, this.staticCall,
				new Object()
				{
					@Override
					public int hashCode()
					{
						return Arrays.hashCode( CallKey.this.argTypes );
					}
				}
			}
		);
	}

	@Override
	public boolean equals( Object other )
	{
		if( !( other instanceof CallKey ) )
			return false;

		CallKey key = (CallKey)other;
		if( key.type != this.type )
			return false;
		if( key.staticCall != this.staticCall )
			return false;

		if( key.name == null )
		{
			if( this.name != null )
				return false;
		}
		else
		{
			if( !key.name.equals( this.name ) )
				return false;
		}

		int len = key.argTypes.length;
		if( this.argTypes.length != len )
			return false;

		for( int i = 0; i < len; i++ )
			if( key.argTypes[ i ] != this.argTypes[ i ] )
				return false;

		return true;
	}
}
