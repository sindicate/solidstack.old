package solidstack.script;


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
	public Object evaluate( Context context )
	{
		Object result = null;
		while( Operation.isTrue( this.condition.evaluate( context ) ) )
			result = this.left.evaluate( context );
		return result;
	}
}
