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

import java.io.Writer;
import java.util.Map;

import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled template.
 * 
 * @author René M. de Bloois
 */
public class GroovyTemplate extends Template
{
	private Closure template;


	/**
	 * Constructor.
	 * 
	 * @param source The source code of the template. This is the template translated to the source code of the desired language.
	 * @param directives The directives found in the template text.
	 */
	public GroovyTemplate( String source, Directive[] directives )
	{
		super( source, directives );
	}

	/**
	 * Apply this template.
	 * 
	 * @param params The parameters to be applied.
	 * @param writer The result of applying this template is written to this writer.
	 */
	@Override
	public void apply( Map< String, ? > params, Writer writer )
	{
		Closure template = (Closure)this.template.clone();
		template.setDelegate( params );
		template.call( createEncodingWriter( writer ) );
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
	 * Sets the Groovy closure.
	 * 
	 * @param closure The Groovy closure.
	 */
	protected void setClosure( Closure closure )
	{
		this.template = closure;
	}
}
