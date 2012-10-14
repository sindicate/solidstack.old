package solidstack.script;

import java.util.List;

public class Function extends Expression
{
	private List<String> parameters;
	private Expression block;

	public Function( List<String> parameters, Expression block )
	{
		this.parameters = parameters;
		this.block = block;
	}

	@Override
	public Object evaluate( Context context )
	{
		return new FunctionInstance( this.parameters, this.block );
	}
}
