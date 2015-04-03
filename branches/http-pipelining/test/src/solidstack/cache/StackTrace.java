package solidstack.cache;


/**
 * Wrapper to enable the current stack trace of a thread to be logged.
 *
 * @author René de Bloois
 */
public class StackTrace extends Throwable
{
	private static final long serialVersionUID = 1L;

	private Thread thread;

	/**
	 * @param thread The thread of which the stack trace needs to be logged.
	 */
	public StackTrace( Thread thread )
	{
		super( "" );
		this.thread = thread;
	}

	@Override
	@SuppressWarnings( "all" )
	public Throwable fillInStackTrace()
	{
		return this;
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		return this.thread.getStackTrace();
	}
}
