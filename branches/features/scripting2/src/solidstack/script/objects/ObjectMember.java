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

import solidstack.script.Script;
import solidstack.script.java.Java;


public class ObjectMember
{
	private Object object;
	private String name;

	public ObjectMember( Object object, String name )
	{
		this.object = object;
		this.name = name;
	}

	public Object getObject()
	{
		return this.object;
	}

	public String getName()
	{
		return this.name;
	}

	public Object invoke( Object... args )
	{
		return Script.toScript( Java.invoke( this.object, this.name, args ) );
	}

	public Object get()
	{
		return Script.toScript( Java.get( this.object, this.name ) );
	}
}