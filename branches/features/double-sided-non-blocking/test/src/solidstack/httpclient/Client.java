package solidstack.httpclient;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread
{
	private String hostname;

	public Client( String hostname ) throws IOException
	{
		this.hostname = hostname;
	}

	public Request connect() throws IOException
	{
		Socket socket = new Socket( this.hostname, 80 );
		Request request = new Request( socket.getOutputStream(), socket.getInputStream() );
		return request;
	}
}
