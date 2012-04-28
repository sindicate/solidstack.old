package solidstack.hyperdb;

import solidstack.httpserver.nio.Server;


public class MainNio
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		Server server = new Server( 80 );
		server.addApplication( new HyperDBApplication() );

		server.run();
	}
}
