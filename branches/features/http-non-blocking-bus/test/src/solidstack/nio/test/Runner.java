package solidstack.nio.test;

import solidstack.httpclient.Request;
import solidstack.httpclient.Response;
import solidstack.httpclient.ResponseProcessor;
import solidstack.httpclient.nio.Client;
import solidstack.io.FatalIOException;
import solidstack.nio.Dispatcher;

public class Runner
{
	private int counter;
	private Dispatcher dispatcher;
	private Client client;
	private Request request;

	private int started;
	int completed;
	int timedOut;
	int failed;

	private long last = System.currentTimeMillis();

	public Runner( Dispatcher dispatcher )
	{
		this.dispatcher = dispatcher;
		this.client = new Client( "localhost", 8001, dispatcher );
		this.request = new Request( "/" );
//		this.request.setHeader( "Host", "www.nu.nl" );
	}

	public void trigger()
	{
//		System.out.println( "triggered " + this.counter++ );

		try
		{
			this.client.request( this.request, new ResponseProcessor()
			{
				public void timeout()
				{
					Runner.this.timedOut ++;
				}

				public void process( Response response )
				{
					if( response.getStatus() == 200 )
						Runner.this.completed ++;
					else
						Runner.this.failed ++;
	//				System.out.println( "Response" );
				}
			} );
		}
		catch( FatalIOException e )
		{
			this.failed ++;
		}

		this.started ++;

		long now = System.currentTimeMillis();
		if( now - this.last >= 1000 )
		{
			this.last += 1000;

			System.out.println( "Complete: " + this.completed + ", failed: " + this.failed + ", timeout: " + this.timedOut + ", sockets: " + this.client.getSocketCount() );
		}
	}
}
