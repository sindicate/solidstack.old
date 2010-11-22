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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO Change detection
public class QueryManager
{
	static final private Logger LOGGER = LoggerFactory.getLogger( QueryManager.class );

	protected String packag;
	protected String packageSlashed = ""; // when setPackage is not called
	protected boolean reloading;
	protected Map< String, QueryTemplate > queries = new HashMap< String, QueryTemplate >();

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
		LOGGER.info( "Reloading = [" + reloading + "]" );
		this.reloading = reloading;
	}

	synchronized public QueryTemplate getQueryTemplate( String path )
	{
		LOGGER.debug( "getQuery [" + path + "]" );

		Assert.isTrue( !path.startsWith( "/" ), "path should not start with a /" );

		QueryTemplate query = this.queries.get( path );

		UrlResource resource = null;
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
			LOGGER.debug( resource.toString() + ", lastModified: " + new Date( lastModified ) + " (" + lastModified + ")" );
		}

		// If reloading == true and resource is changed, clear current query
		if( this.reloading )
			if( query != null && query.getLastModified() > 0 )
				if( resource != null && resource.exists() && lastModified > query.getLastModified() )
				{
					LOGGER.info( resource.toString() + " changed, reloading" );
					query = null;
				}

		// Compile the query if needed
		if( query == null )
		{
			if( !resource.exists() )
			{
				String error = resource.toString() + " not found";
				throw new QueryNotFoundException( error );
			}

			LOGGER.info( "Loading " + resource.toString() );

			Reader reader = new InputStreamReader( resource.getInputStream() ); // TODO Character set
			query = QueryCompiler.compile( reader, this.packageSlashed + path, lastModified );

			this.queries.put( path, query );
		}

		return query;
	}

	public Query bind( String path, Map< String, Object > args )
	{
		QueryTemplate query = getQueryTemplate( path );
		return query.bind( args );
	}
}
