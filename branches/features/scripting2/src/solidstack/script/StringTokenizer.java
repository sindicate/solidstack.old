package solidstack.script;

import solidstack.io.SourceReader;

public class StringTokenizer extends ScriptTokenizer
{
	private boolean found;

	public StringTokenizer( SourceReader in )
	{
		super( in );
	}

	public String getFragment()
	{
		this.found = false;
		StringBuilder result = this.result;
		result.setLength( 0 );

		while( true )
		{
			int ch = this.in.read();
			if( ch == -1 )
				return result.toString();

			switch( ch )
			{
				case '$':
					int ch2 = this.in.read();
					if( ch2 == '{' )
					{
						this.found = true;
						return result.toString();
					}
					this.in.push( ch2 );
					break;

				case '\\':
					ch2 = this.in.read();
					if( ch2 != '$' )
						this.in.push( ch2 );
					else
						ch = '$';
					break;
			}

			result.append( (char)ch );
		}
	}

	public boolean foundExpression()
	{
		return this.found;
	}
}
