package solidstack.io;

public class FatalURISyntaxException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public FatalURISyntaxException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public FatalURISyntaxException( String message )
	{
		super( message );
	}

	public FatalURISyntaxException( Throwable cause )
	{
		super( cause );
	}
}
