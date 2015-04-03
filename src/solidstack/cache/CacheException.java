package solidstack.cache;


/**
 * Thrown whenever something went wrong in the cache.
 *
 * @author René de Bloois
 */
public class CacheException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message Detail message.
	 * @param cause The cause.
	 */
	public CacheException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
