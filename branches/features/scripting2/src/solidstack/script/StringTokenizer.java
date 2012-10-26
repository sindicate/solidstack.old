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

import solidstack.io.PushbackReader;
import solidstack.io.SourceReader;


/**
 * A tokenizer for a super string.
 *
 * @author René de Bloois
 */
public class StringTokenizer extends ScriptTokenizer
{
	private boolean found;


	/**
	 * @param in The source reader.
	 */
	public StringTokenizer( SourceReader in )
	{
		super( in );
	}

	/**
	 * Read a string fragment. A fragment ends at a ${ or at the end of the string. After calling this method the method
	 * {@link #foundExpression()} indicates if an ${ expression was encountered while reading the last fragment.
	 *
	 * @return The fragment. Maybe empty but never null.
	 */
	public String getFragment()
	{
		this.found = false;
		StringBuilder result = clearBuffer();
		PushbackReader in = getIn();

		while( true )
		{
			int ch = in.read();
			if( ch == -1 )
				return result.toString();

			switch( ch )
			{
				case '$':
					int ch2 = in.read();
					if( ch2 == '{' )
					{
						this.found = true;
						return result.toString();
					}
					in.push( ch2 );
					break;

				case '\\':
					ch2 = in.read();
					if( ch2 != '$' )
						in.push( ch2 );
					else
						ch = '$';
					break;
			}

			result.append( (char)ch );
		}
	}

	/**
	 * @return True if a ${ expression was found while reading the last fragment.
	 */
	public boolean foundExpression()
	{
		return this.found;
	}
}
