package solidstack.nio;

import solidstack.httpclient.ResponseProcessor;

public class Timeout
{
	private ResponseProcessor processor;
	private long timeout;

	public Timeout( ResponseProcessor processor, long timeout )
	{
		this.processor = processor;
		this.timeout = timeout;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public ResponseProcessor getProcessor()
	{
		return this.processor;
	}
}
