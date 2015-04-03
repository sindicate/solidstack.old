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

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.DefaultServlet;
import solidstack.httpserver.Servlet;
import solidstack.httpserver.SessionFilter;
import solidstack.httpserver.SltServlet;
import solidstack.template.TemplateLoader;

public class HyperDBApplication extends ApplicationContext
{
	public HyperDBApplication()
	{
		TemplateLoader loader = new TemplateLoader();
		loader.setTemplatePath( "classpath:/solidstack/hyperdb" );
		loader.setReloading( true );

		Servlet sltServlet = new SltServlet( loader );

		// TODO I think we need a ResourceLoader for the DefaultServlet that functions similarly to the TemplateLoader
		// TODO Some slt's should not be accessible from the outside

//		context.forward( "/schemas/([^/]*)/tables", "schema", new ForwardServlet( "/slt", sltServlet ) );

//		registerServlet( "/schemas/([^/]*)/tables/([^/]*)/recordcount", "schema table", new TableRecordCountServlet() );
//		registerServlet( "/schemas/([^/]*)/tables/([^/]*)", "schema table", new TableServlet() );
//		registerServlet( "/schemas/([^/]*)/tables", "schema", new IncludeServlet( "/slt/tables" ) );
//		registerServlet( "/schemas/([^/]*)/views/([^/]*)", "schema view", new ViewServlet() );
//		registerServlet( "/schemas/([^/]*)/views", "schema", new IncludeServlet( "/slt/views" ) );
//		registerServlet( "/schemas", new IncludeServlet( "/slt/schemas" ) );

		registerServlet( "/databases/([^/]*)/([^/]*)/schemas/([^/]*)/views/([^/]*)", "database user schema view", new ViewServlet() );
		registerServlet( "/databases/([^/]*)/([^/]*)/schemas/([^/]*)/views", "database user schema", new IncludeServlet( "/slt/views" ) );
		registerServlet( "/databases/([^/]*)/([^/]*)/schemas/([^/]*)/tables/([^/]*)", "database user schema table", new TableServlet() );
		registerServlet( "/databases/([^/]*)/([^/]*)/schemas/([^/]*)/tables", "database user schema", new IncludeServlet( "/slt/tables" ) );
		registerServlet( "/databases/([^/]*)/([^/]*)/schemas", "database user", new IncludeServlet( "/slt/schemas" ) );
		registerServlet( "/databases/([^/]*)/connect", "database", new ConnectServlet() );
		registerServlet( "/databases", new IncludeServlet( "/slt/databases" ) );

		registerServlet( "/bi", new BiServlet() );
		registerServlet( "", new RootServlet() );
		registerServlet( "/slt(/.*)", "path", sltServlet );
		registerServlet( ".*", new DefaultServlet() );

		registerFilter( ".*", new SessionFilter() );
//		context.registerFilter( ".*", new CompressionFilter() );
	}
}
