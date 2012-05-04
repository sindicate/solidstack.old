package solidstack.nio;


public class Timeout
{
	private ReadListener listener;
	private long timeout;

	public Timeout( ReadListener listener, long timeout )
	{
		this.listener = listener;
		this.timeout = timeout;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public ReadListener getListener()
	{
		return this.listener;
	}
}
