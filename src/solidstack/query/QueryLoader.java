/*--
 * Copyright 2006 Ren� M. de Bloois
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

import solidstack.template.TemplateLoader;


/**
 * Reads, compiles and caches the queries.
 *
 * @author Ren� M. de Bloois
 */
public class QueryLoader
{
	private TemplateLoader templateLoader;
	private boolean locked;


	/**
	 * Constructor.
	 */
	public QueryLoader()
	{
		this.templateLoader = new TemplateLoader();
	}

	/**
	 * Constructor which uses an existing TemplateLoader.
	 *
	 * @param templateLoader The template loader to use.
	 */
	public QueryLoader( TemplateLoader templateLoader )
	{
		this.templateLoader = templateLoader;
		this.locked = true;
	}

	/**
	 * Configures the path which acts as the root of the template files.
	 *
	 * @param path The path.
	 */
	public void setTemplatePath( String path )
	{
		checkLock();
		this.templateLoader.setTemplatePath( path );
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
		this.templateLoader.setReloading( reloading );
	}

	/**
	 * Sets the default scripting language of the templates. The default scripting language is used when a "language" directive is missing in the template.
	 *
	 * @param language The default scripting language of the templates.
	 */
	public void setDefaultLanguage( String language )
	{
		checkLock();
		this.templateLoader.setDefaultLanguage( language );
	}

	/**
	 * Returns a {@link Query}.
	 *
	 * @param path The path of the query relative to the configured root package.
	 * @return The {@link Query}.
	 */
	public Query getQuery( String path )
	{
		return new Query( this.templateLoader.getTemplate( path ) );
	}

	private void checkLock()
	{
		if( this.locked )
			throw new IllegalStateException( "The TemplateLoader must be configured directly." );
	}

	// TODO Missing defineTemplate()
}
