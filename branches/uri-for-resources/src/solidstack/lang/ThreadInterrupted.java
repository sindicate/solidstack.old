package solidstack.lang;


/**
 * <p>
 * Throw this {@link Error} whenever a thread is interrupted by {@link InterruptedException}.
 * </p>
 * <p>
 * This is subclass of {@link Error} because lots of applications catch {@link Exception} and discard it.
 * </p>
 * <p>
 * You could also use {@link ThreadDeath}, but then you can't distinguish between the use of {@link Thread#interrupt()}
 * and {@link Thread#stop()}.
 * </p>
 *
 * @see ThreadDeath for more information about cleanly interrupting a thread.
 * @author René de Bloois
 */
public class ThreadInterrupted extends Error
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param cause The cause of this interruption.
	 */
	public ThreadInterrupted( Throwable cause )
	{
		super( cause.getMessage(), cause );
	}
}
