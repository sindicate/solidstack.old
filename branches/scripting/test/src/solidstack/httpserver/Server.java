package solidstack.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import solidstack.lang.SystemException;
import solidstack.lang.ThreadInterrupted;


public class Server extends Thread
{
	private int port;
	private ApplicationContext application; // TODO Make this a Map

	public Server( int port )
	{
		this.port = port;
	}

	public void addApplication( ApplicationContext application )
	{
		this.application = application;
	}

	@Override
	public void run()
	{
		try
		{
			ServerSocket server = new ServerSocket( this.port );
			server.setSoTimeout( 2000 );
			while( !Thread.interrupted() )
			{
				try
				{
					Socket socket = server.accept();
					// TODO Threadpool
					Handler handler = new Handler( socket, this.application );
					handler.start();
				}
				catch( SocketTimeoutException e )
				{
					// Just to give the opportunity to check Thread.interrupted()
				}
			}
			// TODO Clean up the threadpool, I think it should also use a threadgroup
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public void interruptAndJoin()
	{
		interrupt();
		try
		{
			join();
		}
		catch( InterruptedException e )
		{
			throw new ThreadInterrupted();
		}
	}
}
