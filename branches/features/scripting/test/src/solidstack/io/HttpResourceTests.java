package solidstack.io;

import java.io.IOException;
import java.net.URL;

import org.testng.annotations.Test;

import solidstack.httpserver.ApplicationContext;
import solidstack.httpserver.DefaultServlet;
import solidstack.httpserver.Server;

public class HttpResourceTests
{
	@Test
	static public void test1() throws InterruptedException, IOException
	{
		Server server = new Server( 123 );
		server.addApplication( new ApplicationContext()
		{
			{
				registerServlet( ".*", new DefaultServlet() );
			}
		} );

		server.start();

		URL url = new URL( "http://localhost:123/styles.css" );
		Object content = url.getContent();
		System.out.println( content.getClass() );

		server.interruptAndJoin();
	}
}
