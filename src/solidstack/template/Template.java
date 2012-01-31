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

package solidstack.template;

import groovy.lang.Closure;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled template.
 * 
 * @author Ren� M. de Bloois
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


	/**
	 * Constructor.
	 * 
	 * @param source The source code of the template. This is the template translated to the source code of the desired language.
	 * @param directives The directives found in the template text.
	 */
	public Template( String source, Directive[] directives )
	{
		this.source = source;
		this.directives = directives;
	}

	/**
	 * Sets the manager of the template. The template needs this to access the MIME type registry.
	 * 
	 * @param manager A template manager.
	 */
	protected void setManager( TemplateManager manager )
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
		template.call( createEncodingWriter( writer ) );
	}

	/**
	 * Applies this template and writes the result to an OutputStream. The character set used is the one configured in
	 * the template. If none is configured the default character encoding of the operating system is used.
	 * 
	 * @param params The parameters to be applied.
	 * @param out The result of applying this template is written to this OutputStream.
	 */
	// TODO Test this one
	// TODO Use default per MIME type too, then use the encoding of the source file, then the operating system
	public void apply( Map< String, ? > params, OutputStream out )
	{
		Writer writer;
		if( this.charSet != null )
		{
			try
			{
				writer = new OutputStreamWriter( out, this.charSet );
			}
			catch( java.io.UnsupportedEncodingException e )
			{
				throw new UnsupportedEncodingException( e.getMessage() );
			}
		}
		else
			writer = new OutputStreamWriter( out );
		Closure template = (Closure)this.template.clone();
		template.setDelegate( params );
		template.call( createEncodingWriter( writer ) );
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

	/**
	 * Returns the EncodingWriter for the configured MIME type.
	 * 
	 * @param writer The writer to write to.
	 * @return The EncodingWriter.
	 */
	protected EncodingWriter createEncodingWriter( Writer writer )
	{
		if( this.contentType != null )
		{
			EncodingWriterFactory factory = this.manager.getWriterFactory( this.contentType );
			if( factory != null )
				return factory.createWriter( writer );
		}
		return new NoEncodingWriter( writer );
	}

	/**
	 * Returns the content type of this template.
	 * 
	 * @return The content type of this template.
	 */
	public String getContentType()
	{
		return this.contentType;
	}

	/**
	 * Returns the output character set of this template.
	 * 
	 * @return The output character set of this template.
	 */
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

	/**
	 * Returns the source code for the template.
	 * 
	 * @return The source code for the template.
	 */
	protected String getSource()
	{
		return this.source;
	}

	/**
	 * Returns the Groovy closure.
	 * 
	 * @return The Groovy closure.
	 */
	protected Closure getClosure()
	{
		return this.template;
	}

	/**
	 * Returns the directive attribute with the given directive name and attribute name.
	 * 
	 * @param name The name of the directive.
	 * @param attribute The name of the attribute.
	 * @return The directive.
	 */
	public Directive getDirective( String name, String attribute )
	{
		if( this.directives == null )
			return null;
		for( Directive directive : this.directives )
			if( directive.getName().equals( name ) && directive.getAttribute().equals( attribute ) )
				return directive;
		return null;
	}

	/**
	 * Sets the content type of the template.
	 * 
	 * @param contentType The content type.
	 */
	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}

	/**
	 * Sets the character set of the output of the template.
	 * 
	 * @param charSet The character set.
	 */
	public void setCharSet( String charSet )
	{
		this.charSet = charSet;
	}

	/**
	 * Sets the last modified timestamp of the template.
	 * 
	 * @param lastModified The last modified timestamp.
	 */
	protected void setLastModified( long lastModified )
	{
		this.lastModified = lastModified;
	}

	/**
	 * Sets the Groovy closure.
	 * 
	 * @param closure The Groovy closure.
	 */
	protected void setClosure( Closure closure )
	{
		this.template = closure;
	}

	/**
	 * Removes the templates source from memory.
	 */
	protected void clearSource()
	{
		this.source = null;
	}
}
