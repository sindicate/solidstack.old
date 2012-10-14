package solidstack.script.operations;

import java.util.Map;

import solidstack.script.Expression;
import solidstack.script.Operation;


public class IfExp extends Operation
{
	public IfExp( Expression left, Expression middle, Expression right)
	{
		super( left, middle, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		if( isTrue( left ) )
			return evaluateAndUnwrap( this.middle, context );
		return evaluateAndUnwrap( this.right, context );
	}
}
