package solidstack.script.operations;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Operation;


public class Multiply extends Operation
{
	public Multiply( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		Object right = evaluateAndUnwrap( this.right, context );
		Assert.isInstanceOf( left, BigDecimal.class );
		Assert.isInstanceOf( right, BigDecimal.class );
		return ( (BigDecimal)left ).multiply( (BigDecimal)right );
	}
}