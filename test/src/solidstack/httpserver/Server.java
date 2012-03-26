package solidstack.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server
{
	protected boolean nio = false;

	public void start( ApplicationContext context, int port ) throws IOException
	{
		ServerSocket server = new ServerSocket( port );
		while( true )
		{
			Socket socket = server.accept();
			Handler handler = new Handler( socket, context );
			handler.start();
		}
	}
}
