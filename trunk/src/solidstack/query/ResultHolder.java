package solidstack.query;


/**
 * Needed to store results from an anonymous inner class method.
 * 
 * @param <T> The type of the result.
 */
public class ResultHolder< T >
{
	private T value;

	/**
	 * Set the result.
	 * 
	 * @param value The value of the result.
	 */
	public void set( T value )
	{
		this.value = value;
	}

	/**
	 * Gets the result.
	 * 
	 * @return The result.
	 */
	public T get()
	{
		return this.value;
	}
}
