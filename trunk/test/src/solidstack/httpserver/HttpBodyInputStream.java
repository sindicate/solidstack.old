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

import solidstack.lang.Assert;


public class HttpBodyInputStream extends InputStream
{
	private InputStream in;
	private int length; // TODO long?
	private int read;

	public HttpBodyInputStream( InputStream in, int length )
	{
		Assert.notNull( in );
		this.in = in;
		this.length = length;
	}

	@Override
	public int read() throws IOException
	{
		if( this.read >= this.length )
			return -1;
		int result = this.in.read();
		Assert.isTrue( result >= 0 );
		this.read++;
		return result;
	}
}
