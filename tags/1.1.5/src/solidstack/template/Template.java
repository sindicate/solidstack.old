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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * A compiled template.
 * 
 * @author René M. de Bloois
 */
public class Template
{
	private final Closure template;
	private long lastModified;

	/**
	 * Constructor.
	 * 
	 * @param closure A groovy closure which is the compiled version of the template.
	 * @param lastModified The last modified time stamp of the file that contains the template.
	 */
	public Template( Closure closure, long lastModified )
	{
		this.template = closure;
		this.lastModified = lastModified;
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
		template.setDelegate( new TemplateDelegate( params ) );
		template.call( writer );
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
	 * Returns the last modification time stamp for the file that contains the template.
	 * 
	 * @return The last modification time stamp for the file that contains the template.
	 */
	public long getLastModified()
	{
		return this.lastModified;
	}
}
