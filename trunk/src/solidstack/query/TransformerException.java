package solidstack.query;


/**
 * Something has gone wrong with parsing the SQL template.
 * 
 * @author René M. de Bloois
 */
// TODO Rename to ParseException?
public class TransformerException extends RuntimeException
{
	private int lineNumber;

	/**
	 * Constructor.
	 * 
	 * @param message The message.
	 * @param lineNumber The line number where the problem occurred.
	 */
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
