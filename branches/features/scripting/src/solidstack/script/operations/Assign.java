package solidstack.script.operations;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Operation;


public class Assign extends Operation
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object right = evaluateAndUnwrap( this.right, context );
		Assert.isInstanceOf( right, BigDecimal.class, "Unexpected " + this.right.getClass() );
		// TODO Replace this with Value?
		this.left.assign( context, right );
		return right;
	}
}
