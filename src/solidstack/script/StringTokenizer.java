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

package solidstack.script;

import solidstack.io.PushbackReader;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;


/**
 * A tokenizer for a super string.
 *
 * @author Ren� de Bloois
 */
public class StringTokenizer extends ScriptTokenizer
{
	private boolean found;
	private boolean doubleQuoted;


	/**
	 * @param in The source reader.
	 */
	public StringTokenizer( SourceReader in, boolean doubleQuoted )
	{
		super( in );
		this.doubleQuoted = doubleQuoted;
	}

	public StringTokenizer( PushbackReader in, boolean doubleQuoted )
	{
		super( in );
		this.doubleQuoted = doubleQuoted;
	}

	/**
	 * Read a string fragment. A fragment ends at a ${ or at the end of the string. After calling this method the method
	 * {@link #foundExpression()} indicates if an ${ expression was encountered while reading the last fragment.
	 *
	 * @return The fragment. Maybe empty but never null.
	 */
	public Fragment getFragment()
	{
		this.found = false;
		StringBuilder result = clearBuffer();
		PushbackReader in = getIn();
		SourceLocation location = in.getLocation();

		while( true )
		{
			int ch = in.read();
			switch( ch )
			{
				case -1:
					if( this.doubleQuoted )
						throw new SourceException( "Missing '\"'", in.getLastLocation() );
					return new Fragment( location, result.toString() ); // end-of-input: we're done

				case '"':
					if( this.doubleQuoted )
						return new Fragment( location, result.toString() ); // end-of-string: we're done
					break;

				case '$':
					int ch2 = in.read();
					if( ch2 == '{' )
					{
						this.found = true;
						return new Fragment( location, result.toString() );
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

	/**
	 * A fragment.
	 */
	static public class Fragment
	{
		private SourceLocation location;
		private String value;

		Fragment( SourceLocation location, String value )
		{
			this.location = location;
			this.value = value;
		}

		/**
		 * @return The location of the token in the source.
		 */
		public SourceLocation getLocation()
		{
			return this.location;
		}

		/**
		 * @return The value of the token.
		 */
		public String getValue()
		{
			return this.value;
		}

		public int length()
		{
			return this.value.length();
		}
	}
}
