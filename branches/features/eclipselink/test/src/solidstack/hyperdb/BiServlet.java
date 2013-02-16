package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseWriter;
import solidstack.httpserver.Servlet;


public class BiServlet implements Servlet
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