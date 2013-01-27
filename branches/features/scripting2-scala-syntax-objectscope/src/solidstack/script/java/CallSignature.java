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




public class CallSignature
{
	public Class type; // Class of the object to call or, in the case of a static call, the class to call
	public String name; // The name of the method, null for constructor
	public boolean staticCall; // Calling the class?
	public Class[] argTypes; // Types of the arguments.

	public CallSignature( Class type, String name, boolean staticCall, Class[] argTypes )
	{
		this.type = type;
		this.name = name;
		this.staticCall = staticCall;
		this.argTypes = argTypes;
	}

	@Override
	public int hashCode()
	{
        int result = 1;
        result = 31 * result + this.type.hashCode();
        result *= 31; if( this.name != null ) result += this.name.hashCode();
        result = 31 * result + ( this.staticCall ? 1231 : 1237 );
        if( this.argTypes != null )
        {
            result *= 31; // TODO Maybe if property becomes a separate boolean we don't need this one
	        for( Class<?> type : this.argTypes )
	        {
	            result *= 31;
	            if( type != null ) result += type.hashCode();
	        }
        }
        return result;
	}

	@Override
	public boolean equals( Object other )
	{
		if( !( other instanceof CallSignature ) ) return false;

		CallSignature key = (CallSignature)other;

		if( key.type != this.type ) return false;
		if( key.staticCall != this.staticCall ) return false;
		if( key.name == null ? this.name != null : !key.name.equals( this.name ) ) return false;
		if( key.argTypes == null ) return this.argTypes == null;
		if( this.argTypes == null ) return false;

		int len = key.argTypes.length;
		if( this.argTypes.length != len )
			return false;

		for( int i = 0; i < len; i++ )
			if( key.argTypes[ i ] != this.argTypes[ i ] )
				return false;

		return true;
	}
}
