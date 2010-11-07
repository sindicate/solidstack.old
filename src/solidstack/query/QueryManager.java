/*--
 * Copyright 2006 René M. de Bloois
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

package solidstack.query;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


// TODO Change detection
public class QueryManager
{
	static final private Logger __LOGGER = Logger.getLogger( QueryManager.class );

	protected String packag;
	protected String packageSlashed = ""; // when setPackage is not called
	protected boolean reloading;
	protected Map< String, CompiledQuery > queries = new HashMap();

	public void setPackage( String packag )
	{
		Assert.isTrue( !packag.startsWith( "." ) && !packag.endsWith( "." ), "path should not start or end with a ." );

		this.packag = packag;
		if( packag.length() > 0 )
			this.packageSlashed = packag.replaceAll( "\\.", "/" ) + "/";
		else
			this.packageSlashed = "";
	}

	public void setReloading( boolean reloading )
	{
		__LOGGER.info( "Reloading = [" + reloading + "]" );
		this.reloading = reloading;
	}

	synchronized public CompiledQuery getQuery( String path )
	{
		__LOGGER.debug( "getQuery [" + path + "]" );

		Assert.isTrue( !path.startsWith( "/" ), "path should not start with a /" );

		try
		{
			CompiledQuery query = this.queries.get( path );

			Resource resource = null;
			long lastModified = 0;

			// If reloading == true or query not initialized yet, get the resource
			if( this.reloading || query == null )
			{
				String file = this.packageSlashed + path + ".gsql";
				//ClassLoader loader = Thread.currentThread().getContextClassLoader();
				ClassLoader loader = getClass().getClassLoader();
				URL url = loader.getResource( file );
				if( url == null )
					throw new QueryNotFoundException( file + " not found in classpath" );
				resource = new UrlResource( url );
				try
				{
					lastModified = resource.getFile().lastModified();
				}
				catch( FileNotFoundException e )
				{
					// Appearantly the resource is packed in a jar of some kind.
					// lastModified stays 0, which means no reloading will be tried.
				}
				__LOGGER.debug( resource.getDescription() + ", lastModified: " + new Date( lastModified ) + " (" + lastModified + ")" );
			}

			// If reloading == true and resource is changed, clear current query
			if( this.reloading )
				if( query != null && query.lastModified > 0 )
					if( resource != null && resource.exists() && lastModified > query.lastModified )
					{
						__LOGGER.info( resource.getDescription() + " changed, reloading" );
						query = null;
					}

			// Compile the query if needed
			if( query == null )
			{
				if( !resource.exists() )
				{
					String error = resource.getDescription() + " not found";
					throw new QueryNotFoundException( error );
				}

				__LOGGER.info( "Loading " + resource.getDescription() );

				Reader reader = new InputStreamReader( resource.getInputStream() ); // TODO Character set
				query = QueryCompiler.compile( reader, this.packageSlashed + path, lastModified );

				this.queries.put( path, query );
			}

			return query;
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public Query getQuery( String path, Map args )
	{
		CompiledQuery query = getQuery( path );
		return query.params( args );
	}
}
