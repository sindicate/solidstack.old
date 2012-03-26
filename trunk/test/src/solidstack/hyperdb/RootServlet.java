package solidstack.hyperdb;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseWriter;
import solidstack.httpserver.Servlet;
import solidstack.util.Pars;


public class RootServlet implements Servlet
{
	public void call( RequestContext context, Pars params )
	{
//		new TemplateServlet().call( context, new Parameters( params ).put( "title", null ).put( "body", new Servlet()
//		{
//			public void call( RequestContext request, Parameters params )
//			{
//				ResponseWriter writer = request.getResponse().getWriter();
//				writer.write( "<a href=\"/tables\">tables</a>\n" );
//			}
//		}));

		ResponseWriter writer = context.getResponse().getWriter();
		writer.write( "<a href=\"/slt/tables\">tables</a>\n" );
	}
}
