package solidstack.script;



public class Parenthesis extends Expression
{
	private Expression expression;

	public Parenthesis( Expression expression )
	{
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return this.expression;
	}

	@Override
	public Object evaluate( Context context )
	{
		return this.expression.evaluate( context );
	}

	@Override
	public Object assign( Context context, Object value )
	{
		return this.expression.assign( context, value );
	}
}
