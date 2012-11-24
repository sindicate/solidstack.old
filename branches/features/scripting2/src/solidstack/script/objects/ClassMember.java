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

import solidstack.script.java.Java;


public class ClassMember implements Member
{
	private Class type;
	private String name;

	public ClassMember( Class type, String name )
	{
		this.type = type;
		this.name = name;
	}

	public Object getType()
	{
		return this.type;
	}

	public String getName()
	{
		return this.name;
	}

	public Object invoke( Object... args )
	{
		return Util.toScript( Java.invokeStatic( this.type, this.name, args ) );
	}

	public Object get()
	{
		return Util.toScript( Java.getStatic( this.type, this.name ) );
	}
}
