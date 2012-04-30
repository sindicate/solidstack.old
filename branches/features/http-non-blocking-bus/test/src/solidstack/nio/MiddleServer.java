package solidstack.nio;

import java.io.IOException;

import solidstack.httpserver.nio.Server;


public class MiddleServer
{
	static public Dispatcher dispatcher;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException
	{
		dispatcher = new Dispatcher();

		Server server = new Server( dispatcher, 8002 );
		server.setApplication( new MiddleServerApplication() );

		dispatcher.run();
	}
}
