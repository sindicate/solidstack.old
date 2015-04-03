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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Request
{
//	static protected int count = 1;

	// TODO GET or POST
	private String path;
	private Map< String, List< String > > headers = new HashMap< String, List<String> >();

	public Request( String path )
	{
		setPath( path );
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setHeader( String name, String value )
	{
		List< String > values = new ArrayList< String >();
		values.add( value );
		getHeaders().put( name, values );
	}

	public String getHeader( String name )
	{
		List< String > values = getHeaders().get( name );
		if( values == null )
			return null;
		Assert.isTrue( !values.isEmpty() );
		if( values.size() > 1 )
			throw new IllegalStateException( "Found more than 1 value for the header " + name );
		return values.get( 0 );
	}

	public Map< String, List< String > > getHeaders()
	{
		return this.headers;
	}
}
