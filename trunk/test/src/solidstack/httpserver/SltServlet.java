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

package solidstack.httpserver;

import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.template.TemplateNotFoundException;
import solidstack.util.Pars;

public class SltServlet implements Servlet
{
	private TemplateLoader loader;

	public SltServlet( TemplateLoader loader )
	{
		this.loader = loader;
	}

	public void call( RequestContext context )
	{
		// TODO / should be allowed after fixing the other todo
		String url = context.getRequest().getParameter( "path" );
//		if( url.startsWith( "/" ) )
//			url = url.substring( 1 );

		Template template;
		try
		{
			template = this.loader.getTemplate( url );
		}
		catch( TemplateNotFoundException e )
		{
			context.getResponse().setStatusCode( 404, "Not found" );
			return;
		}

		// Don't want to catch TemplateNotFoundException in the lines below
		Pars pars = new Pars( "session", context.getSession(), "request", context.getRequest(), "args", context.getArgs() ); // TODO response
		template.apply( pars, context.getResponse().getWriter() );

//		url = url.replaceAll( "[\\\\/]", "." );
//		url = url.replaceAll( "[\\.-]", "_" );
//		request.applicationContext.callJsp( url, request );
	}
}
