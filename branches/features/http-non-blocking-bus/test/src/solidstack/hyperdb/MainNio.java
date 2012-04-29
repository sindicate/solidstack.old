package solidstack.hyperdb;

import java.io.IOException;

import solidstack.httpserver.nio.Server;
import solidstack.nio.Dispatcher;


public class MainNio
{
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException
	{
		Dispatcher selector = new Dispatcher();

		Server server = new Server( selector, 80 );
		server.setApplication( new HyperDBApplication() );

		selector.run();
	}
}
