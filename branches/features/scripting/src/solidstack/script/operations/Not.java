package solidstack.script.operations;

import java.util.Map;

import org.springframework.util.Assert;

import solidstack.script.Expression;
import solidstack.script.Operation;


public class Not extends Operation
{
	public Not( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isNull( this.left );
		Object right = evaluateAndUnwrap( this.right, context );
		return !Operation.isTrue( right );
	}
}
