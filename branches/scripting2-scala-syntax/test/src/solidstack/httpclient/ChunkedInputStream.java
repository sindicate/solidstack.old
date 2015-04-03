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

package solidstack.httpclient;

import java.io.IOException;
import java.io.InputStream;

import solidstack.httpserver.HttpException;
import solidstack.lang.Assert;

public class ChunkedInputStream extends InputStream
{
	private InputStream in;
	private int left;

	public ChunkedInputStream( InputStream in )
	{
		this.in = in;
	}

	@Override
	public int read() throws IOException
	{
		if( this.left > 0 )
		{
			int result = this.in.read();
			this.left--;
			if( this.left == 0 )
			{
				int ch = this.in.read();
				if( ch == '\r' )
					ch = this.in.read();
				Assert.isTrue( ch == '\n' );
			}
			return result;
		}

		if( this.left == -1 )
			return -1;

		String line = readLine();
		this.left = Integer.parseInt( line, 16 );
		if( this.left == 0 )
		{
			int ch = this.in.read();
			if( ch == '\r' )
				ch = this.in.read();
			Assert.isTrue( ch == '\n' );
			this.left = -1;
			return -1;
		}

		return read();
	}

	private String readLine() throws IOException
	{
		StringBuilder result = new StringBuilder();
		while( true )
		{
			int ch = this.in.read();
			if( ch == -1 )
				throw new HttpException( "Unexpected end of line" );
			if( ch == '\r' )
				continue;
			if( ch == '\n' )
				return result.toString();
			result.append( (char)ch );
		}
	}
}
