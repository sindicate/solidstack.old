package solidstack.hyperdb;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.DefaultServlet;
import solidstack.httpserver.Server;
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

//		context.registerServlet( "/tables/([^/]*)/recordcount", "tablename", new TableRecordCountServlet() );
//		context.registerServlet( "/tables/([^/]*)", "tablename", new TableServlet() );
//		context.registerServlet( "/tables", new TablesServlet() );
//		context.registerServlet( "/test", new TestServlet() );
		context.registerServlet( "", new RootServlet() );
		context.registerServlet( "/slt(/.*)", "path", new SltServlet( loader ) );
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
