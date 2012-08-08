package solidstack.hyperdb;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import solidstack.httpserver.HttpException;
import solidstack.httpserver.HttpResponse;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseOutputStream;
import solidstack.httpserver.Servlet;


public class RootServlet implements Servlet
{
	public HttpResponse call( RequestContext context )
	{
//		new TemplateServlet().call( context, new Parameters( params ).put( "title", null ).put( "body", new Servlet()
//		{
//			public void call( RequestContext request, Parameters params )
//			{
//				ResponseWriter writer = request.getResponse().getWriter();
//				writer.write( "<a href=\"/tables\">tables</a>\n" );
//			}
//		}));

		return new HttpResponse()
		{
			@Override
			public void write( ResponseOutputStream out )
			{
				out.setContentType( "text/html", "UTF-8" );
				try
				{
					Writer writer = new OutputStreamWriter( out, "UTF-8" );
					writer.write( "<a href=\"/databases\">databases</a>\n" );
					writer.flush();
				}
				catch( IOException e )
				{
					throw new HttpException( e );
				}
			}
		};
	}
}
