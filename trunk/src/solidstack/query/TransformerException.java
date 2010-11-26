package solidstack.query;

public class TransformerException extends RuntimeException
{
	private int lineNumber;

	public TransformerException( String message, int lineNumber )
	{
		super( message );
		this.lineNumber = lineNumber;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ", at line " + this.lineNumber;
	}
}
