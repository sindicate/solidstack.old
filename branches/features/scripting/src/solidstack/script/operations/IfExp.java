package solidstack.script.operations;


import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.Operation;


public class IfExp extends Operation
{
	public IfExp( Expression left, Expression middle, Expression right)
	{
		super( left, middle, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		if( isTrue( left ) )
			return evaluateAndUnwrap( this.middle, context );
		return evaluateAndUnwrap( this.right, context );
	}
}
