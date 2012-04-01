package solidstack.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server
{
	private ApplicationContext application; // TODO Make this a Map

	public void addApplication( ApplicationContext application )
	{
		this.application = application;
	}

	public void start( int port ) throws IOException
	{
		ServerSocket server = new ServerSocket( port );
		while( true )
		{
			Socket socket = server.accept();
			// TODO Threadpool
			Handler handler = new Handler( socket, this.application );
			handler.start();
		}
	}
}
