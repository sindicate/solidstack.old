/*--
 * Copyright 2010 René M. de Bloois
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
 * Something has gone wrong during reading of the source.
 *
 * @author René M. de Bloois
 */
public class SourceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private SourceLocation location;

	/**
	 * Constructor.
	 *
	 * @param message The message.
	 * @param location The file location where the problem is located.
	 */
	public SourceException( String message, SourceLocation location )
	{
		super( message );
		this.location = location;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ", at " + this.location;
	}

	/**
	 * Returns the file location where the problem is located.
	 *
	 * @return The file location where the problem is located.
	 */
	public SourceLocation getLocation()
	{
		return this.location;
	}
}
