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

package solidstack.template;


/**
 * Something has gone wrong with parsing the SQL template.
 * 
 * @author René M. de Bloois
 */
public class ParseException extends RuntimeException
{
	private int lineNumber;

	/**
	 * Constructor.
	 * 
	 * @param message The message.
	 * @param lineNumber The line number where the problem occurred.
	 */
	public ParseException( String message, int lineNumber )
	{
		super( message );
		this.lineNumber = lineNumber;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ", at line " + this.lineNumber;
	}
}
