package solidstack.script.operations;

import java.util.Map;

import solidstack.script.Expression;
import solidstack.script.Operation;


public class LessThan extends Operation
{
	public LessThan( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		Object right = evaluateAndUnwrap( this.right, context );
		return Operation.compare( left, right ) < 0;
	}
}
