package solidstack.script;

import java.util.Map;

public class While extends Expression
{
	private Expression condition;
	private Expression left;

	public While( Expression condition, Expression left )
	{
		this.condition = condition;
		this.left = left;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object result = null;
		while( Operation.isTrue( this.condition.evaluate( context ) ) )
			result = this.left.evaluate( context );
		return result;
	}
}
