/*--
 * Copyright 2011 Ren� M. de Bloois
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

package solidstack.template;

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
import solidstack.query.UrlResource;


/**
 * Reads, compiles and caches the queries.
 * 
 * Usage:
 * 
 * <pre>
 *    Map&lt; String, Object &gt; args = new HashMap&lt; String, Object &gt;();
 *    args.put( &quot;arg1&quot;, arg1 );
 *    args.put( &quot;arg2&quot;, arg2 );
 *    Template template = templateManager.getTemplate( &quot;path/filename&quot; );
 *    String result = template.apply( args );</pre>
 * 
 * <p>
 * The {@link #getTemplate(String)} call looks in the classpath for a file 'path/filename' in the package configured
 * with {@link #setPackage(String)}.
 * </p>
 * 
 * @author Ren� M. de Bloois
 */
public class TemplateManager
{
	static private Logger log = LoggerFactory.getLogger( TemplateManager.class );

	private String packageSlashed = ""; // when setPackage is not called
	private boolean reloading;
	private Map< String, Template > templates = new HashMap< String, Template >();

	/**
	 * Configures the package which is the root of the template files.
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
		log.info( "Reloading = [" + reloading + "]" );
		this.reloading = reloading;
	}

	/**
	 * Returns the compiled {@link Template} with the given path.
	 * 
	 * @param path The path of the template.
	 * @return The {@link Template}.
	 */
	synchronized public Template getTemplate( String path )
	{
		log.debug( "getTemplate [" + path + "]" );

		Assert.isTrue( !path.startsWith( "/" ), "path should not start with a /" );

		Template template = this.templates.get( path );

		UrlResource resource = null;

		// If reloading == true and resource is changed, clear current query
		if( this.reloading )
			if( template != null && template.getLastModified() > 0 )
			{
				resource = getResource( path );
				if( resource.exists() && resource.getLastModified() > template.getLastModified() )
				{
					log.info( resource.toString() + " changed, reloading" );
					template = null;
				}
			}

		// Compile the query if needed
		if( template == null )
		{
			if( resource == null )
				resource = getResource( path );

			if( !resource.exists() )
			{
				String error = resource.toString() + " not found";
				throw new TemplateNotFoundException( error );
			}

			log.info( "Loading " + resource.toString() );

			try
			{
				Reader reader = new InputStreamReader( resource.getInputStream(), "UTF-8" );
				template = TemplateTransformer.compile( reader, this.packageSlashed + path, resource.getLastModified() );
			}
			catch( UnsupportedEncodingException e )
			{
				throw new SystemException( e );
			}

			this.templates.put( path, template );
		}

		return template;
	}

	/**
	 * Returns the {@link UrlResource} with the given path.
	 * 
	 * @param path The path of the resource.
	 * @return The {@link UrlResource}.
	 */
	public UrlResource getResource( String path )
	{
		String file = this.packageSlashed + path;
		//ClassLoader loader = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = getClass().getClassLoader();
		URL url = loader.getResource( file );
		if( url == null )
			throw new TemplateNotFoundException( file + " not found in classpath" );

		UrlResource resource = new UrlResource( url );

		if( log.isDebugEnabled() )
			log.debug( resource.toString() + ", lastModified: " + new Date( resource.getLastModified() ) + " (" + resource.getLastModified() + ")" );

		return resource;
	}
}
