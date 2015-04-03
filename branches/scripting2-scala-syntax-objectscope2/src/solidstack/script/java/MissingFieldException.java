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



public class MissingFieldException extends Exception
{
	private Object object;
	private Class<?> type;
	private String name;

	public MissingFieldException( Object object, Class<?> type, String name )
	{
		this.object = object;
		this.type = type;
		this.name = name;
	}

	@Override
	public String getMessage()
	{
		StringBuilder result = new StringBuilder( "No such field: " );
		result.append( this.object == null ? "static " : "" );
		result.append( this.type.getName() ).append( '.' ).append( this.name );
		return result.toString();
	}
}
