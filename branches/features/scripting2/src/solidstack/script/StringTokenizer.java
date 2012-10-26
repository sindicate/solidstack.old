package solidstack.script;

import solidstack.io.PushbackReader;
import solidstack.io.SourceReader;


/**
 * A tokenizer for a super string.
 *
 * @author René de Bloois
 */
public class StringTokenizer extends ScriptTokenizer
{
	private boolean found;


	/**
	 * @param in The source reader.
	 */
	public StringTokenizer( SourceReader in )
	{
		super( in );
	}

	/**
	 * Read a string fragment. A fragment ends at a ${ or at the end of the string. After calling this method the method
	 * {@link #foundExpression()} indicates if an ${ expression was encountered while reading the last fragment.
	 *
	 * @return The fragment. Maybe empty but never null.
	 */
	public String getFragment()
	{
		this.found = false;
		StringBuilder result = clearBuffer();
		PushbackReader in = getIn();

		while( true )
		{
			int ch = in.read();
			if( ch == -1 )
				return result.toString();

			switch( ch )
			{
				case '$':
					int ch2 = in.read();
					if( ch2 == '{' )
					{
						this.found = true;
						return result.toString();
					}
					in.push( ch2 );
					break;

				case '\\':
					ch2 = in.read();
					if( ch2 != '$' )
						in.push( ch2 );
					else
						ch = '$';
					break;
			}

			result.append( (char)ch );
		}
	}

	/**
	 * @return True if a ${ expression was found while reading the last fragment.
	 */
	public boolean foundExpression()
	{
		return this.found;
	}
}
