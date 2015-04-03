package solidstack.cache;


/**
 * Thrown whenever there is a timeout in the cache.
 *
 * @author René de Bloois
 */
public class CacheTimeoutException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message Detail message.
	 */
	public CacheTimeoutException( String message )
	{
		super( message );
	}
}
