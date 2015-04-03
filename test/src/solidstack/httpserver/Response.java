package solidstack.httpserver;

import java.io.OutputStream;


/*
 * Types of responses:
 * 1. Ready, does not need input anymore ->
 * 2. Ready, needs input still
 * 3. Not ready, does not need input anymore
 */
abstract public class Response
{
	private boolean ready;
	private ResponseListener listener;

	synchronized public void setListener( ResponseListener listener )
	{
		this.listener = listener;
	}

	abstract public void write( OutputStream out );

	public boolean needsInput()
	{
		return false;
	}

	public boolean isReady()
	{
		return this.ready;
	}

	synchronized public void ready()
	{
		if( this.listener != null )
			this.listener.responseIsReady( this );
		else
			setReady();
	}

	public void setReady()
	{
		this.ready = true;
	}
}
