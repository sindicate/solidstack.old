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
import java.util.HashMap;
import java.util.Map;

import solidstack.httpserver.HttpBodyInputStream;
import solidstack.lang.SystemException;


public class Response
{
	private String httpVersion;
	private int status;
	private String reason;

	private InputStream in;
	private HttpBodyInputStream bodyIn;
	private Map<String, String> headers = new HashMap<String, String>();
	// TODO Multivalued headers

	public Response()
	{
	}

	public void setHttpVersion( String httpVersion )
	{
		this.httpVersion = httpVersion;
	}

	public String getHttpVersion()
	{
		return this.httpVersion;
	}

	public void setStatus( int status )
	{
		this.status = status;
	}

	public int getStatus()
	{
		return this.status;
	}

	public void setReason( String reason )
	{
		this.reason = reason;
	}

	public String getReason()
	{
		return this.reason;
	}

	public void addHeader( String name, String value )
	{
		this.headers.put( name, value );
	}

	public String getHeader( String name )
	{
		return this.headers.get( name ); // TODO Case insensitivity
	}

	public Map<String, String> getHeaders()
	{
		return this.headers;
	}

	public void print()
	{
		byte[] buffer = new byte[ 4096 ];
		try
		{
			int len = this.bodyIn.read( buffer );
			while( len >= 0 )
			{
				System.out.write( buffer, 0, len );
				len = this.bodyIn.read( buffer );
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public void setInputStream( InputStream in )
	{
		this.in = in;
	}

	public InputStream getInputStream()
	{
		return this.in;
	}
}
