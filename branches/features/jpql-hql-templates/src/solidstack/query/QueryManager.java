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

import solidstack.template.TemplateManager;


/**
 * Reads, compiles and caches the queries.
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
	 * Configures the package which acts as the root of the template files.
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
	// TODO Need reload delay to prevent to much file checking
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
	 * @param path The path of the query relative to the configured root package.
	 * @return The {@link Query}.
	 */
	public Query getQuery( String path )
	{
		// TODO Remove this extension.
		// TODO We need another extension, g from Groovy does not cut it anymore. We need a nice name. xxx.sql.solt?
		return new Query( this.templateManager.getTemplate( path + ".gsql" ) );
	}

	private void checkLock()
	{
		if( this.locked )
			throw new IllegalStateException( "The TemplateManager must be configured directly." );
	}
}
