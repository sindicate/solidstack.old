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

public class CompressionFilter implements Filter
{
	public void call( RequestContext context, FilterChain chain )
	{
		context.getResponse().setHeader( "Content-Encoding", "gzip" );
		GZipResponse gzipResponse = new GZipResponse( context.getResponse() );
		try
		{
			chain.call( new RequestContext( context.getRequest(), gzipResponse, context.applicationContext ) );
			gzipResponse.finish();
		}
		catch( FatalSocketException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			gzipResponse.finish();
			throw e;
		}
		catch( Error e )
		{
			gzipResponse.finish();
			throw e;
		}
	}
}
