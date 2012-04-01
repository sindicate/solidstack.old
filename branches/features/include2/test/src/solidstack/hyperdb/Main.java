package solidstack.hyperdb;

import java.io.IOException;

import solidstack.httpserver.Server;

public class Main
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		Server server = new Server();
		server.addApplication( new HyperDBApplication() );

		try
		{
			server.start( 80 );
		}
		catch( IOException e )
		{
			e.printStackTrace( System.err );
		}
	}
}
