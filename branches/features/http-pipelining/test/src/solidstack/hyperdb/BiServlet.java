package solidstack.hyperdb;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import solidstack.httpserver.HttpResponse;
import solidstack.httpserver.RequestContext;
import solidstack.httpserver.ResponseOutputStream;
import solidstack.httpserver.Servlet;
import solidstack.io.FatalIOException;


public class BiServlet implements Servlet
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
				out.setContentType( "text/html", null );
				Writer writer = new OutputStreamWriter( out );
				try
				{
					writer.write( "<a href=\"/databases\">databases</a>\n" );
					writer.flush();
				}
				catch( IOException e )
				{
					throw new FatalIOException( e );
				}
			}
		};
	}
}
