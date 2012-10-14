package solidstack.script;

import java.util.List;

public class FunctionInstance
{
	private List<String> parameters;
	private Expression block;

	public FunctionInstance( List<String> parameters, Expression block )
	{
		this.parameters = parameters;
		this.block = block;
	}

	public Object call( Context context, List<Object> pars )
	{
		int count = this.parameters.size();
		if( count != pars.size() )
			throw new ScriptException( "Parameter count mismatch" );

		context = new SubContext( context );

		// TODO If we keep the Link we get output parameters!
		for( int i = 0; i < count; i++ )
		{
			Object value = pars.get( i );
			if( value instanceof Value )
				value = ( (Value)value ).get();
			context.set( this.parameters.get( i ), value );
		}
		return this.block.evaluate( context );
	}
}
