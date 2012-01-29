package solidstack.template;


/**
 * The Character Encoding is not supported.
 * 
 * @author René de Bloois
 */
public class UnsupportedEncodingException extends RuntimeException
{
	/**
	 * Constructor.
	 * 
	 * @param message The message.
	 */
	public UnsupportedEncodingException( String message )
	{
		super( message );
	}
}
