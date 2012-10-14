package solidstack.script;

import java.util.List;
import java.util.Map;

public class FunctionSpec extends Expression
{
	private List<String> parameters;
	private Expression block;

	public FunctionSpec( List<String> parameters, Expression block )
	{
		this.parameters = parameters;
		this.block = block;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return new FunctionInstance( this.parameters, this.block );
	}
}
