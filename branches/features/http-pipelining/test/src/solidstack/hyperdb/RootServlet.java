package solidstack.hyperdb;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import solidstack.httpserver.RequestContext;
import solidstack.httpserver.Response;
import solidstack.httpserver.ResponseOutputStream;
import solidstack.httpserver.Servlet;


public class RootServlet implements Servlet
{
	public Response call( RequestContext context )
	{
//		new TemplateServlet().call( context, new Parameters( params ).put( "title", null ).put( "body", new Servlet()
//		{
//			public void call( RequestContext request, Parameters params )
//			{
//				ResponseWriter writer = request.getResponse().getWriter();
//				writer.write( "<a href=\"/tables\">tables</a>\n" );
//			}
//		}));

		return new Response()
		{
			@Override
			public void write( ResponseOutputStream out ) throws IOException
			{
				out.setContentType( "text/html", null );
				Writer writer = new OutputStreamWriter( out );
				writer.write( "<a href=\"/databases\">databases</a>\n" );
				writer.flush();
			}
		};
	}
}
