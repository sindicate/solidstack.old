package solidstack.hyperdb;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.DefaultServlet;
import solidstack.httpserver.Server;
import solidstack.httpserver.Servlet;
import solidstack.httpserver.SltServlet;
import solidstack.template.TemplateLoader;

public class Main
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		ApplicationContext context = new ApplicationContext();

		TemplateLoader loader = new TemplateLoader();
		loader.setTemplatePath( "classpath:/solidstack/hyperdb" );
		loader.setReloading( true );

		Servlet sltServlet = new SltServlet( loader );

		// TODO I think we need a ResourceLoader for the DefaultServlet that functions similarly to the TemplateLoader
		// TODO Some slt's should not be accessible from the outside

//		context.forward( "/schemas/([^/]*)/tables", "schema", new ForwardServlet( "/slt", sltServlet ) );

		context.registerServlet( "/schemas/([^/]*)/tables/([^/]*)/recordcount", "schema table", new TableRecordCountServlet() );
		context.registerServlet( "/schemas/([^/]*)/tables/([^/]*)", "schema table", new TableServlet() );
		context.registerServlet( "/schemas/([^/]*)/tables", "schema", new IncludeServlet( "/slt/tables" ) );
		context.registerServlet( "/schemas/([^/]*)/views/([^/]*)", "schema view", new ViewServlet() );
		context.registerServlet( "/schemas/([^/]*)/views", "schema", new IncludeServlet( "/slt/views" ) );
		context.registerServlet( "/schemas", new IncludeServlet( "/slt/schemas" ) );
		context.registerServlet( "", new RootServlet() );
		context.registerServlet( "/slt(/.*)", "path", sltServlet );
		context.registerServlet( ".*", new DefaultServlet() );

//		context.registerFilter( ".*", new CompressionFilter() );

		context.setJspBase( "solidbase.http.hyperdb" );

		try
		{
			new Server().start( context, 80 );
		}
		catch( Throwable t )
		{
			t.printStackTrace( System.err );
		}
	}
}
