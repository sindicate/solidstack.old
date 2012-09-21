package solidstack.io;

import java.io.Reader;
import java.io.StringReader;


/**
 * A string resource.
 *
 * @author René de Bloois
 */
public class StringResource extends Resource
{
	private String data;

	/**
	 * @param data The string.
	 */
	public StringResource( String data )
	{
		this.data = data;
	}

	@Override
	public boolean supportsReader()
	{
		return true;
	}

	@Override
	public Reader newReader()
	{
		return new StringReader( this.data );
	}
}
