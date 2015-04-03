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

import java.io.IOException;
import java.io.InputStream;

import solidstack.io.FatalIOException;

public class UrlEncodedParser
{
	private InputStream in;
	private int length;
	private int pos;

	public UrlEncodedParser( InputStream in, int length )
	{
		this.in = in;
		this.length = length;
	}

	public String getParameter()
	{
		if( this.pos >= this.length )
			return null;

		StringBuilder result = new StringBuilder();
		while( true )
		{
			if( this.pos >= this.length )
				return result.toString();

			int ch;
			try
			{
				ch = this.in.read();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
			this.pos++;
			if( ch == '=' )
				return result.toString();

			result.append( (char)ch );
		}
	}

	public String getValue()
	{
		if( this.pos >= this.length )
			return null;

		StringBuilder result = new StringBuilder();
		while( true )
		{
			if( this.pos >= this.length )
				return result.toString();

			int ch;
			try
			{
				ch = this.in.read();
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
			this.pos++;
			if( ch == '&' )
				return result.toString();

			result.append( (char)ch );
		}
	}
}
