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

package solidstack.template.groovy;

import groovy.lang.Closure;

import java.util.Map;

import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateContext;

/**
 * A compiled Groovy template.
 *
 * @author René M. de Bloois
 */
public class GroovyTemplate extends Template
{
	private Closure closure;


	/**
	 * @param closure The Groovy closure.
	 */
	public GroovyTemplate( Closure closure )
	{
		this.closure = closure;
	}

	@Override
	public void apply( Map< String, Object > args, EncodingWriter writer )
	{
		Closure template = (Closure)this.closure.clone();
//		template.setResolveStrategy( Closure.DELEGATE_FIRST );
		GroovyTemplateContext context = new GroovyTemplateContext( this, writer, args );
		template.setDelegate( context );
		template.call( new GroovyConvertingWriter( writer ) );
	}

	@Override
	public void apply( TemplateContext parent, Map< String, Object > args )
	{
		Closure template = (Closure)this.closure.clone();
//		template.setResolveStrategy( Closure.DELEGATE_FIRST );
		GroovyTemplateContext context = new GroovyTemplateContext( this, parent, args );
		template.setDelegate( context );
		template.call( new GroovyConvertingWriter( context.getWriter() ) );
	}
}
