package solidstack.script;

import java.util.Map;

public class If extends Expression
{
	private Expression condition;
	private Expression left;
	private Expression right;

	public If( Expression condition, Expression left, Expression right )
	{
		this.condition = condition;
		this.left = left;
		this.right = right;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		if( Operation.isTrue( this.condition.evaluate( context ) ) )
			return this.left.evaluate( context );
		if( this.right != null )
			return this.right.evaluate( context );
		return null;
	}
}
