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
import java.io.OutputStream;

import solidstack.lang.Assert;


public class ResponseOutputStream extends OutputStream
{
	protected OutputStream out;
	protected Response response;
	protected byte[] buffer = new byte[ 8192 ];
	protected int pos;
	protected boolean committed;

	public ResponseOutputStream()
	{

	}

	public ResponseOutputStream( Response response, OutputStream out )
	{
		this.response = response;
		this.out = out;
	}

	@Override
	public void write( byte[] b, int off, int len )
	{
		try
		{
			if( this.committed )
			{
//				System.out.write( b, off, len );
				this.out.write( b, off, len );
			}
			else if( this.buffer.length - this.pos < len )
			{
				this.response.writeHeader( this.out );
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b, off, len );
				this.out.write( b, off, len );
				this.committed = true;
			}
			else
			{
				System.arraycopy( b, off, this.buffer, this.pos, len );
				this.pos += len;
			}
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
			if( this.committed )
			{
//				System.out.write( b );
				this.out.write( b );
			}
			else if( this.buffer.length - this.pos < b.length )
			{
				this.response.writeHeader( this.out );
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b );
				this.out.write( b );
				this.committed = true;
			}
			else
			{
				System.arraycopy( b, 0, this.buffer, this.pos, b.length );
				this.pos += b.length;
			}
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
			if( this.committed )
			{
//				System.out.write( b );
				this.out.write( b );
			}
			else if( this.buffer.length - this.pos < 1 )
			{
				this.response.writeHeader( this.out );
//				System.out.write( this.buffer, 0, this.pos );
				this.out.write( this.buffer, 0, this.pos );
//				System.out.write( b );
				this.out.write( b );
				this.committed = true;
			}
			else
			{
				this.buffer[ this.pos ] = (byte)b;
				this.pos ++;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void close()
	{
		commit();
		try
		{
			this.out.close();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	private void commit()
	{
		try
		{
			if( !this.committed )
			{
				this.response.writeHeader( this.out );
				// The outputstream may be changed at this point
				this.out.write( this.buffer, 0, this.pos );
				this.committed = true;
			}
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	@Override
	public void flush()
	{
		if( !this.committed )
			commit();
		try
		{
			this.out.flush();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
	}

	public void clear()
	{
		Assert.isFalse( this.committed );
		this.pos = 0;
	}
}
