package solidstack.cache;

public class CacheTimeoutException extends RuntimeException
{
	public CacheTimeoutException( String message )
	{
		super( message );
	}
}
