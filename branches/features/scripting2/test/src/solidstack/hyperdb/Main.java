package solidstack.hyperdb;

import solidstack.httpserver.Server;

public class Main
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
