package solidstack.script;

import java.util.List;
import java.util.Map;

public class FunctionInstance
{
	private List<String> parameters;
	private Expression block;

	public FunctionInstance( List<String> parameters, Expression block )
	{
		this.parameters = parameters;
		this.block = block;
	}

	public Object call( Map<String, Object> context, List<Object> pars )
	{
		int count = this.parameters.size();
		if( count != pars.size() )
			throw new ScriptException( "Parameter count mismatch" );
		// TODO Create subcontext
		for( int i = 0; i < count; i++ )
			context.put( this.parameters.get( i ), pars.get( i ) );
		return this.block.evaluate( context );
	}
}
