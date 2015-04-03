/*--
 * Copyright 2012 René M. de Bloois
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

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

import solidstack.io.FatalURISyntaxException;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.util.Pars;

public class GroovyTemplateDelegate implements GroovyObject
{
	private GroovyTemplate template;
	private Object parameters;
	private EncodingWriter writer;

	public GroovyTemplateDelegate( GroovyTemplate template, Object parameters, EncodingWriter writer )
	{
		this.template = template;
		this.parameters = parameters;
		this.writer = writer;
	}

	public void include( Map< String, Object > args )
	{
		String path = (String)args.get( "template" );
		if( !path.startsWith( "/" ) )
		{
			// TODO Make util
			try
			{
				URI uri = new URI( this.template.getPath() );
				uri = uri.resolve( path );
				path = uri.getPath();
			}
			catch( URISyntaxException e )
			{
				throw new FatalURISyntaxException( e );
			}
		}
		TemplateLoader loader = this.template.getLoader();
		Template template = loader.getTemplate( path );

		Pars pars = new Pars( this.parameters ).set( "args", args );
		template.apply( pars, this.writer );
	}

	public Object getProperty( String property )
	{
		return InvokerHelper.getProperty( this.parameters, property );
	}

	public Object invokeMethod( String name, Object args )
	{
		return InvokerHelper.invokeMethod( this.parameters, name, args );
	}

	public void setProperty( String property, Object newValue )
	{
		InvokerHelper.setProperty( this.parameters, property, newValue );
	}

	public MetaClass getMetaClass()
	{
		throw new UnsupportedOperationException();
	}

	public void setMetaClass( MetaClass metaClass )
	{
		throw new UnsupportedOperationException();
	}
}
