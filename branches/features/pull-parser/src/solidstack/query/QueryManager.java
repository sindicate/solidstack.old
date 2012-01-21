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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;
import solidbase.io.ResourceFactory;
import solidstack.Assert;


/**
 * Reads, compiles and caches the queries.
 * 
 * Usage:
 * 
 * <pre>
 *    Map&lt; String, Object &gt; args = new HashMap&lt; String, Object &gt;();
 *    args.put( &quot;arg1&quot;, arg1 );
 *    args.put( &quot;arg2&quot;, arg2 );
 *    Query query = queryManager.bind( &quot;path/filename&quot;, args );
 *    List&lt; Map&lt; String, Object &gt;&gt; result = query.listOfMaps( connection );</pre>
 * 
 * <p>
 * The {@link #bind(String, Map)} call looks in the classpath for a file 'path/filename.gsql' in the package configured
 * with {@link #setPackage(String)}.
 * </p>
 * 
 * <p>
 * The arguments in the map given to the bind call can be any type as long as the template produces something that the
 * JDBC driver understands.
 * </p>
 * 
 * <p>
 * See {@link Query} for a description of what you can do with the query returned by the bind call.
 * </p>
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

		Resource resource = null;

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
				throw new QueryNotFoundException( resource.toString() + " not found" );

			LOGGER.info( "Loading " + resource.toString() );

			LineReader reader;
			try
			{
				reader = new BOMDetectingLineReader( resource );
			}
			catch( FileNotFoundException e )
			{
				throw new QueryNotFoundException( resource.toString() + " not found" );
			}
			query = QueryTransformer.compile( reader, this.packageSlashed + path, resource.getLastModified() );

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
	public Resource getResource( String path )
	{
//		if( LOGGER.isDebugEnabled() )
//			LOGGER.debug( resource.toString() + ", lastModified: " + new Date( resource.getLastModified() ) + " (" + resource.getLastModified() + ")" );
		return ResourceFactory.getResource( "classpath:" + this.packageSlashed + path + ".gsql" );
//		if( url == null )
//			throw new QueryNotFoundException( file + " not found in classpath" );
	}

	/**
	 * Binds the arguments and the template and returns the {@link Query}.
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
