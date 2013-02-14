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

package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseWriter;
import solidstack.httpserver.Servlet;


public class RootServlet implements Servlet
{
	public void call( RequestContext context )
	{
//		new TemplateServlet().call( context, new Parameters( params ).put( "title", null ).put( "body", new Servlet()
//		{
//			public void call( RequestContext request, Parameters params )
//			{
//				ResponseWriter writer = request.getResponse().getWriter();
//				writer.write( "<a href=\"/tables\">tables</a>\n" );
//			}
//		}));

		context.getResponse().setContentType( "text/html", null );
		ResponseWriter writer = context.getResponse().getWriter();

//		try
//		{
//			Thread.sleep( 500 );
//		}
//		catch( InterruptedException e )
//		{
//			throw new ThreadInterrupted();
//		}

		writer.write( "<a href=\"/databases\">databases</a>\n" );
	}
}
