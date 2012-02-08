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

import java.util.Map;

import solidstack.template.TemplateManager;


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
 * The {@link #apply(String, Map)} call looks in the classpath for a file 'path/filename.gsql' in the package configured
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
	private TemplateManager templateManager;
	private boolean locked;


	/**
	 * Constructor.
	 */
	public QueryManager()
	{
		this.templateManager = new TemplateManager();
	}

	/**
	 * Constructor which uses an existing TemplateManager.
	 * 
	 * @param templateManager The template manager to use.
	 */
	public QueryManager( TemplateManager templateManager )
	{
		this.templateManager = templateManager;
		this.locked = true;
	}

	/**
	 * Configures the package which acts the root of the template files.
	 *
	 * @param pkg The package.
	 */
	public void setPackage( String pkg )
	{
		checkLock();
		this.templateManager.setPackage( pkg );
	}

	/**
	 * Enable or disable reloading. When enabled, the lastModified time stamp of the file is used to check if it needs reloading.
	 *
	 * @param reloading When true, the file is reloaded when updated.
	 */
	public void setReloading( boolean reloading )
	{
		checkLock();
		this.templateManager.setReloading( reloading );
	}

	/**
	 * Sets the default scripting language of the templates. The default scripting language is used when a "language" directive is missing in the template.
	 * 
	 * @param language The default scripting language of the templates.
	 */
	public void setDefaultLanguage( String language )
	{
		checkLock();
		this.templateManager.setDefaultLanguage( language );
	}

	/**
	 * Returns a {@link Query}.
	 *
	 * @param path The path of the query.
	 * @return The {@link Query}.
	 */
	public Query getQuery( String path )
	{
		return new Query( this.templateManager.getTemplate( path + ".gsql" ) );
	}

	private void checkLock()
	{
		if( this.locked )
			throw new IllegalStateException( "Can't configure the TemplateManager indirectly." );
	}
}
