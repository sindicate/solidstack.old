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
import java.util.zip.GZIPOutputStream;

public class GZipResponseOutputStream extends ResponseOutputStream
{
	protected Response response;
	protected GZIPOutputStream out;

	public GZipResponseOutputStream( Response response )
	{
		this.response = response;
		try
		{
			this.out = new GZIPOutputStream( response.getOutputStream() );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( byte[] b, int off, int len )
	{
		try
		{
			this.out.write( b, off, len );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( byte[] b )
	{
		try
		{
			this.out.write( b );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void write( int b )
	{
		try
		{
			this.out.write( b );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void close()
	{
		try
		{
			this.out.close();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void flush()
	{
		try
		{
			this.out.flush();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void clear()
	{
		this.response.getOutputStream().clear();
		try
		{
			this.out = new GZIPOutputStream( this.response.getOutputStream() );
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}
}
