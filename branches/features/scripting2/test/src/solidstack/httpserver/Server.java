/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.lang.SystemException;
import solidstack.lang.ThreadInterrupted;


public class Server extends Thread
{
	static final private Logger log = LoggerFactory.getLogger( Server.class );

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
			log.info( "HTTP server listening on port {}", this.port );

			while( !Thread.interrupted() )
			{
				try
				{
					Socket socket = server.accept();
					log.debug( "Incoming socket, starting handler" );
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
