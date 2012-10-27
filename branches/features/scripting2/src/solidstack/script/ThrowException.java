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

package solidstack.script;

public class ThrowException extends RuntimeException
{
	private Object object;

	public ThrowException( Object object )
	{
		this.object = object;
	}

	@Override
	public synchronized Throwable initCause( Throwable cause )
	{
		return null;
	}

	@Override
	public String getMessage()
	{
		return this.object.toString();
	}
}
