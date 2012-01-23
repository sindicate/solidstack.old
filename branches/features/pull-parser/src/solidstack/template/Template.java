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

package solidstack.template;

import groovy.lang.Closure;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import solidstack.SystemException;
import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled template.
 * 
 * @author René M. de Bloois
 */
public class Template
{
	private String source;
	private Directive[] directives;

	private Closure template;
	private String contentType;
	private String charSet;
	private long lastModified;
	private TemplateManager manager;

	public Template( String source, Directive[] directives )
	{
		this.source = source;
		this.directives = directives;
	}

	public void setManager( TemplateManager manager )
	{
		this.manager = manager;
	}

	/**
	 * Apply this template.
	 * 
	 * @param params The parameters to be applied.
	 * @param writer The result of applying this template is written to this writer.
	 */
	public void apply( Map< String, ? > params, Writer writer )
	{
		Closure template = (Closure)this.template.clone();
		template.setDelegate( params );
		template.call( getEncodingWriter( writer ) );
	}

	/**
	 * Apply this template.
	 * 
	 * @param params The parameters to be applied.
	 * @param writer The result of applying this template is written to this writer.
	 */
	// TODO Test this one
	public void apply( Map< String, ? > params, OutputStream out )
	{
		Writer writer;
		if( this.charSet != null )
		{
			try
			{
				writer = new OutputStreamWriter( out, this.charSet );
			}
			catch( UnsupportedEncodingException e )
			{
				throw new SystemException( e ); // TODO Better exception?
			}
		}
		else
			writer = new OutputStreamWriter( out ); // TODO Should we use the encoding from the source file?
		Closure template = (Closure)this.template.clone();
		template.setDelegate( params ); // TODO Escaping should depend on the content type
		template.call( getEncodingWriter( writer ) );
	}

	/**
	 * Apply this template.
	 * 
	 * @param params The parameters to be applied.
	 * @return The result of applying this template.
	 */
	public String apply( Map< String, ? > params )
	{
		StringWriter writer = new StringWriter();
		apply( params, writer );
		return writer.toString();
	}

	protected EncodingWriter getEncodingWriter( Writer writer )
	{
		if( this.contentType != null )
		{
			EncodingWriterFactory factory = this.manager.getWriterFactory( this.contentType );
			if( factory != null )
				return factory.createWriter( writer );
		}
		return new NoEncodingWriter( writer );
	}

	public String getContentType()
	{
		return this.contentType;
	}

	public String getCharSet()
	{
		return this.charSet;
	}

	/**
	 * Returns the last modification time stamp for the file that contains the template.
	 * 
	 * @return The last modification time stamp for the file that contains the template.
	 */
	public long getLastModified()
	{
		return this.lastModified;
	}

	public String getSource()
	{
		return this.source;
	}

	public Directive getDirective( String name, String attribute )
	{
		if( this.directives == null )
			return null;
		for( Directive directive : this.directives )
			if( directive.getName().equals( name ) && directive.getAttribute().equals( attribute ) )
				return directive;
		return null;
	}

	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}

	public void setCharSet( String charSet )
	{
		this.charSet = charSet;
	}

	public void setLastModified( long lastModified )
	{
		this.lastModified = lastModified;
	}

	public void setClosure( Closure closure )
	{
		this.template = closure;
	}

	public void clearSource()
	{
		this.source = null;
	}
}
