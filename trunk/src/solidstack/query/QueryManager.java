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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.SystemException;


/**
 * Manages and caches the queries.
 * 
 * @author René M. de Bloois
 */
public class QueryManager
{
	static final private Logger LOGGER = LoggerFactory.getLogger( QueryManager.class );

	private String packageSlashed = ""; // when setPackage is not called
	private boolean reloading;
	private Map< String, QueryTemplate > queries = new HashMap< String, QueryTemplate >();

	/**
	 * Configures the package which is the root of the gsql file.
	 * 
	 * @param pkg The package.
	 */
	public void setPackage( String pkg )
	{
		Assert.isTrue( !pkg.startsWith( "." ) && !pkg.endsWith( "." ), "package should not start or end with a ." );
		Assert.isTrue( pkg.indexOf('/') < 0 && pkg.indexOf('\\') < 0 , "package should not contain a \\ or /" );

		if( pkg.length() > 0 )
			this.packageSlashed = pkg.replaceAll( "\\.", "/" ) + "/";
		else
			this.packageSlashed = "";
	}

	/**
	 * Enable or disable reloading. When enabled, the lastModified time stamp of the file is used to check if it needs reloading.
	 * 
	 * @param reloading When true, the file is reloaded when updated.
	 */
	public void setReloading( boolean reloading )
	{
		LOGGER.info( "Reloading = [" + reloading + "]" );
		this.reloading = reloading;
	}

	/**
	 * Returns the {@link QueryTemplate} with the given path.
	 * 
	 * @param path The path of the query.
	 * @return The {@link QueryTemplate}.
	 */
	synchronized public QueryTemplate getQueryTemplate( String path )
	{
		LOGGER.debug( "getQuery [" + path + "]" );

		Assert.isTrue( !path.startsWith( "/" ), "path should not start with a /" );

		QueryTemplate query = this.queries.get( path );

		UrlResource resource = null;

		// If reloading == true and resource is changed, clear current query
		if( this.reloading )
			if( query != null && query.getLastModified() > 0 )
			{
				resource = getResource( path );
				if( resource.exists() && resource.getLastModified() > query.getLastModified() )
				{
					LOGGER.info( resource.toString() + " changed, reloading" );
					query = null;
				}
			}

		// Compile the query if needed
		if( query == null )
		{
			if( resource == null )
				resource = getResource( path );

			if( !resource.exists() )
			{
				String error = resource.toString() + " not found";
				throw new QueryNotFoundException( error );
			}

			LOGGER.info( "Loading " + resource.toString() );

			try
			{
				Reader reader = new InputStreamReader( resource.getInputStream(), "ISO-8859-1" );
				query = QueryTransformer.compile( reader, this.packageSlashed + path, resource.getLastModified() );
			}
			catch( UnsupportedEncodingException e )
			{
				throw new SystemException( e );
			}

			this.queries.put( path, query );
		}

		return query;
	}

	/**
	 * Returns the {@link UrlResource} with the given path.
	 * 
	 * @param path The path of the resource.
	 * @return The {@link UrlResource}.
	 */
	public UrlResource getResource( String path )
	{
		String file = this.packageSlashed + path + ".gsql";
		//ClassLoader loader = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = getClass().getClassLoader();
		URL url = loader.getResource( file );
		if( url == null )
			throw new QueryNotFoundException( file + " not found in classpath" );

		UrlResource resource = new UrlResource( url );

		if( LOGGER.isDebugEnabled() )
			LOGGER.debug( resource.toString() + ", lastModified: " + new Date( resource.getLastModified() ) + " (" + resource.getLastModified() + ")" );

		return resource;
	}

	/**
	 * Binds the template and the arguments and returns the {@link Query}.
	 * 
	 * @param path The path of the query.
	 * @param args The arguments.
	 * @return The {@link Query}.
	 */
	public Query bind( String path, Map< String, ? > args )
	{
		QueryTemplate query = getQueryTemplate( path );
		return query.bind( args );
	}
}
