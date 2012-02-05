/*--
 * Copyright 2011 René M. de Bloois
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidbase.io.Resource;
import solidbase.io.ResourceFactory;
import solidstack.Assert;
import solidstack.query.QueryManager;


/**
 * Reads, compiles and caches the templates.
 * 
 * @author René M. de Bloois
 */
public class TemplateManager
{
	static private Logger log = LoggerFactory.getLogger( TemplateManager.class );

	static private final Pattern XML_MIME_TYPE_PATTERN = Pattern.compile( "^[a-z]+/.+\\+xml" ); // TODO http://www.iana.org/assignments/media-types/index.html

	private String packageSlashed = ""; // when setPackage is not called
	private boolean reloading;
	private Map< String, Template > templates = new HashMap< String, Template >();
	private Map< String, Object > mimeTypeMap = new HashMap< String, Object >();


	/**
	 * Constructor.
	 */
	public TemplateManager()
	{
		this.mimeTypeMap.put( "text/xml", XMLEncodingWriter.FACTORY );
		// TODO Put this in a properties file
		this.mimeTypeMap.put( "application/xml", "text/xml" );
		this.mimeTypeMap.put( "text/html", "text/xml" );
	}

	/**
	 * Registers a factory for an encoding writer for a specific MIME type.
	 * 
	 * @param mimeType The MIME type to register the writer for.
	 * @param factory The factory for the writer.
	 */
	public void registerEncodingWriter( String mimeType, EncodingWriterFactory factory )
	{
		synchronized( this.mimeTypeMap )
		{
			this.mimeTypeMap.put( mimeType, factory );
		}
	}

	/**
	 * Registers a MIME type mapping. The first MIME type will be written with the encoding writer of the second MIME type.
	 * 
	 * @param mimeType The MIME type that should be mapped to the other MIME type.
	 * @param encodeAsMimeType The MIME type to map to.
	 */
	public void registerMimeTypeMapping( String mimeType, String encodeAsMimeType )
	{
		synchronized( this.mimeTypeMap )
		{
			this.mimeTypeMap.put( mimeType, encodeAsMimeType );
		}
	}

	/**
	 * Configures the package that will function as the root of the template resources.
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
		log.info( "Reloading = [{}]", reloading );
		this.reloading = reloading;
	}

	/**
	 * Returns the compiled {@link Template} with the given path. Compiled templates are cached in memory. When
	 * {@link #setReloading(boolean)} has been enabled, file change detection will cause the templates to be reloaded
	 * and recompiled.
	 * 
	 * @param path The path of the template.
	 * @return The {@link Template}.
	 */
	// TODO Ability to configure default template language.
	public Template getTemplate( String path )
	{
		log.debug( "getTemplate [{}]", path );
		Assert.isTrue( !path.startsWith( "/" ), "path should not start with a /" );

		synchronized( this.templates )
		{
			Template template = this.templates.get( path );
			Resource resource = null;

			// If reloading == true and resource is changed, clear current query
			if( this.reloading && template != null && template.getLastModified() > 0 )
			{
				resource = getResource( path );
				if( resource.exists() && resource.getLastModified() > template.getLastModified() )
				{
					log.info( "{} changed, reloading", resource );
					template = null;
				}
			}

			// Compile the query if needed
			if( template == null )
			{
				if( resource == null )
					resource = getResource( path );

				if( !resource.exists() )
					throw new TemplateNotFoundException( resource.toString() + " not found" );

				template = getCompiler().compile( resource, this.packageSlashed + path );
				template.setLastModified( resource.getLastModified() );
				template.setManager( this );
				this.templates.put( path, template );
			}

			return template;
		}
	}

	/**
	 * Returns the encoding writer factory for the given MIME type. This method is called by {@link Template}.
	 * 
	 * @param mimeType The MIME type to return the writer factory for.
	 * @return The encoding writer factory for the given MIME type.
	 */
	protected EncodingWriterFactory getWriterFactory( String mimeType )
	{
		synchronized( this.mimeTypeMap )
		{
			Object object = this.mimeTypeMap.get( mimeType );
			while( object != null && object instanceof String )
				object = this.mimeTypeMap.get( object );

			if( object != null )
				return (EncodingWriterFactory)object;

			if( XML_MIME_TYPE_PATTERN.matcher( mimeType ).matches() )
				return getWriterFactory( "text/xml" );
		}

		return null;
	}

	/**
	 * Ability to override which compiler is used to compile the template.
	 * 
	 * @return The compiler to compile the template with.
	 * @see QueryManager
	 */
	protected TemplateCompiler getCompiler()
	{
		return new TemplateCompiler();
	}

	/**
	 * Returns the {@link Resource} with the given path.
	 * 
	 * @param path The path of the resource.
	 * @return The {@link Resource}.
	 */
	private Resource getResource( String path )
	{
		Resource result = ResourceFactory.getResource( "classpath:" + this.packageSlashed + path );
		log.debug( "{}, lastModified: {} ({})", new Object[] { result, new Date( result.getLastModified() ), result.getLastModified() } );
		return result;
	}
}
