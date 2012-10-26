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


public class GZipResponse extends Response
{
	protected Response response;

	public GZipResponse( Response response )
	{
		this.out = new GZipResponseOutputStream( response );
		this.response = response;
	}

	@Override
	public ResponseOutputStream getOutputStream()
	{
		return this.out;
	}

	@Override
	public void setHeader( String name, String value )
	{
		this.response.setHeader( name, value );
	}

	@Override
	public void writeHeader( OutputStream out )
	{
		this.response.writeHeader( out );
	}

	@Override
	public boolean isCommitted()
	{
		return this.response.isCommitted();
	}

	@Override
	public void setStatusCode( int code, String message )
	{
		this.response.setStatusCode( code, message );
	}

	@Override
	public void reset()
	{
		super.reset();
		this.response.reset();
	}

	@Override
	public void finish()
	{
		super.finish();
		try
		{
			( (GZipResponseOutputStream)this.out ).out.finish();
		}
		catch( IOException e )
		{
			throw new HttpException( e );
		}
//		this.response.finish();
	}

	@Override
	public void setContentType( String contentType, String charSet )
	{
		super.setContentType( contentType, charSet );
		this.response.setContentType( contentType, charSet );
	}
}
