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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled template.
 *
 * @author René M. de Bloois
 */
abstract public class Template
{
	private String name;
	private Directive[] directives;

	private String contentType;
	private String charSet;
	private long lastModified;
	private TemplateManager manager;


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
	 * Apply the given parameters to the template and writes the result to the given writer.
	 *
	 * @param params The parameters to be applied.
	 * @param writer The result of applying this template is written to this writer.
	 */
	public void apply( Map< String, Object > params, Writer writer )
	{
		apply( params, createEncodingWriter( writer ) );
	}

	/**
	 * Apply the given parameters to the template and writes the output to the given output stream. The character set used is the one configured in
	 * the template. If none is configured the default character encoding of the operating system is used.
	 *
	 * @param params The parameters to be applied.
	 * @param out The result of applying this template is written to this OutputStream.
	 */
	// TODO Test this one
	// TODO Use default per MIME type too, then use the encoding of the source file, then the operating system
	public void apply( Map< String, Object > params, OutputStream out )
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
		apply( params, writer );
	}

	/**
	 * Apply the given parameters to the template and returns the result as a string.
	 *
	 * @param params The parameters to be applied.
	 * @return The result of applying this template.
	 */
	public String apply( Map< String, Object > params )
	{
		StringWriter writer = new StringWriter();
		apply( params, writer );
		return writer.toString();
	}

	/**
	 * Applies the given parameters to the template and writes the output to the given encoding writer.
	 *
	 * @param params The parameters to apply to the template.
	 * @param writer The writer to write the result to.
	 */
	abstract public void apply( Map< String, Object > params, EncodingWriter writer );

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
	 * Returns the name of the template.
	 *
	 * @return The name of the template.
	 */
	public String getName()
	{
		return this.name;
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
		return getDirective( Arrays.asList( this.directives ), name, attribute );
	}

	/**
	 * Returns the directive attribute with the given directive name and attribute name.
	 *
	 * @param name The name of the directive.
	 * @param attribute The name of the attribute.
	 * @return The directive.
	 */
	static public Directive getDirective( List<Directive> directives, String name, String attribute )
	{
		for( Directive directive : directives )
			if( directive.getName().equals( name ) && directive.getAttribute().equals( attribute ) )
				return directive;
		return null;
	}

	/**
	 * Sets the name of this template.
	 *
	 * @param name The name of this template.
	 */
	protected void setName( String name )
	{
		this.name = name;
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
	 * Sets the directives found in this template.
	 *
	 * @param directives The directives found in this template.
	 */
	protected void setDirectives( Directive[] directives )
	{
		this.directives = directives;
	}
}
