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

package solidstack.io;


/**
 * A {@link RuntimeException} to indicate that something has gone wrong with the URI syntax.
 *
 * @author René de Bloois
 */
public class FatalURISyntaxException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public FatalURISyntaxException( String message, Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * @param message The detail message.
	 */
	public FatalURISyntaxException( String message )
	{
		super( message );
	}

	/**
	 * @param cause The cause.
	 */
	public FatalURISyntaxException( Throwable cause )
	{
		super( cause );
	}
}
