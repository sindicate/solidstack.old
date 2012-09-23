package solidstack.script;

import java.util.Map;


public class Parenthesis extends Expression
{
	private Expression expression;

	public Parenthesis( Expression expression )
	{
		this.expression = expression;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return this.expression.evaluate( context );
	}

	@Override
	public Object assign( Map<String, Object> context, Object value )
	{
		return this.expression.assign( context, value );
	}
}
