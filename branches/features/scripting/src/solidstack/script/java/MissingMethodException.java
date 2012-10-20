package solidstack.script.java;

import solidstack.script.ScriptException;


public class MissingMethodException extends ScriptException
{
	public CallContext context;

	public MissingMethodException( CallContext context )
	{
		this.context = context;
	}

	@Override
	public String getMessage()
	{
		Object object = this.context.getObject();
		String name = this.context.getName();
		Object[] args = this.context.getArgs();
		Class type = object instanceof Class ? (Class)object : object.getClass();
		StringBuilder result = new StringBuilder();
		result.append( "No signature of method: " );
		result.append( object instanceof Class ? "static " : "" );
		result.append( type.getName() );
		result.append( '.' );
		result.append( name );
		result.append( "() is applicable for argument types: (" );
		for( int i = 0; i < args.length; i++ )
		{
			if( i > 0 )
				result.append( ", " );
			if( args[ i ] == null )
				result.append( "null" );
			else
				result.append( args[ i ].getClass().getName()  );
		}
		result.append( ')' );
		return result.toString();
	}
}
