package solidstack.script.operations;


import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.Operation;


public class GreaterThan extends Operation
{
	public GreaterThan( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		Object right = evaluateAndUnwrap( this.right, context );
		return Operation.compare( left, right ) > 0;
	}
}
