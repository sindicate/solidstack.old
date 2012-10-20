package solidstack.script.java;

import java.util.List;

import solidstack.script.ScriptException;


public class ResolverException extends ScriptException
{
	private List< MethodCall > candidates;

	public ResolverException( List candidates )
	{
		this.candidates = candidates;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append( "Cannot resolve method:" );
		for( MethodCall candidate : this.candidates )
		{
			msg.append( "\n\t" ).append( candidate.getDeclaringClass().getName() ).append( '#' ).append( candidate.getName() ).append( '(' );
			Class[] types = candidate.getParameterTypes();
			for( int i = 0; i < types.length; i++ )
			{
				if( i > 0 )
					msg.append( ", " );
				msg.append( types[ i ].getClass().getName()  );
			}
			msg.append( ')' );
		}
		return msg.toString();
	}
}
