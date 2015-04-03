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
 * A location in a resource.
 *
 * @author René de Bloois
 */
public class SourceLocation
{
	private Resource resource;
	private int lineNumber;

	/**
	 * @param resource The resource.
	 * @param lineNumber The line number.
	 */
	public SourceLocation( Resource resource, int lineNumber )
	{
		this.resource = resource;
		this.lineNumber = lineNumber;
	}

	/**
	 * @return The resource.
	 */
	public Resource getResource()
	{
		return this.resource;
	}

	/**
	 * @return The line number.
	 */
	public int getLineNumber()
	{
		return this.lineNumber;
	}

	/**
	 * @return A source location where the line number is decremented by one.
	 */
	public SourceLocation previousLine()
	{
		if( this.lineNumber <= 0 )
			throw new FatalIOException( "There is no previous line" );
		return new SourceLocation( this.resource, this.lineNumber - 1 );
	}

	/**
	 * @return A source location where the line number is incremented by one.
	 */
	public SourceLocation nextLine()
	{
		return new SourceLocation( this.resource, this.lineNumber + 1 );
	}

	/**
	 * @param lineNumber The line number.
	 * @return A source location with the line number overwritten.
	 */
	public SourceLocation lineNumber( int lineNumber )
	{
		return new SourceLocation( this.resource, lineNumber );
	}

	@Override
	public String toString()
	{
		if( this.resource != null )
			return "line " + this.lineNumber + " of file " + this.resource;
		return "line " + this.lineNumber;
	}
}
