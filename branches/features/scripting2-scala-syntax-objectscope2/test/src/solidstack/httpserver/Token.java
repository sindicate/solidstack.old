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

package solidstack.httpserver;

public class Token
{
	/**
	 * The value of the token.
	 */
	protected String value;

	/**
	 * Constructs a new token.
	 *
	 * @param value The value of the token.
	 */
	public Token( String value )
	{
		this.value = value;
//		System.out.println( "Token [" + value + "]" );
	}

	/**
	 * Returns the value of token.
	 *
	 * @return The value of token.
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * Is this token a newline?
	 *
	 * @return True if this token is a newline, false otherwise.
	 */
	public boolean isNewline()
	{
		return this.value.charAt( 0 ) == '\n'; // Assume that if char 0 is a newline then the whole string is just the newline
	}

	/**
	 * Is this token the end-of-input token?
	 *
	 * @return True if this token is the end-of-input token, false otherwise.
	 */
	public boolean isEndOfInput()
	{
		return this.value == null;
	}

	/**
	 * Does a case insensitive comparison with the given string.
	 *
	 * @param s A string to compare the value of this token with.
	 * @return True if the value of this token and the given string are equal (ignoring case), false otherwise.
	 */
	public boolean equals( String s )
	{
		if( this.value == null )
			return false;
		return this.value.equals( s );
	}

//	/**
//	 * The length of the value of this token.
//	 *
//	 * @return Length of the value of this token.
//	 */
//	public int length()
//	{
//		return this.value.length();
//	}

	@Override
	public String toString()
	{
		return this.value;
	}
}
