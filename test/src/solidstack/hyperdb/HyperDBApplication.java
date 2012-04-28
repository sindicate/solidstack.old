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
