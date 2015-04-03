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

import java.io.IOException;

import solidstack.io.FatalIOException;
import solidstack.template.ConvertingWriter;
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
	public void apply( Object params, EncodingWriter writer )
	{
		Closure template = (Closure)this.closure.clone();
		template.setDelegate( new GroovyTemplateDelegate( this, params, writer ) );
		ConvertingWriter out = new GroovyConvertingWriter( writer );
		template.call( out );
		try
		{
			out.flush();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	@Override
	public void apply( TemplateContext context )
	{
		Closure template = (Closure)this.closure.clone();
		template.setDelegate( new GroovyTemplateContextDelegate( context ) );
		ConvertingWriter out = new GroovyConvertingWriter( context.getWriter() );
		template.call( out );
		try
		{
			out.flush();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
