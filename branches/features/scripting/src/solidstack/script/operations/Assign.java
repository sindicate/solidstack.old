package solidstack.script.operations;

import java.math.BigDecimal;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.FunctionInstance;
import solidstack.script.Operation;


public class Assign extends Operation
{
	public Assign( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object right = evaluateAndUnwrap( this.right, context );
		if( right == null || right instanceof BigDecimal || right instanceof String || right instanceof FunctionInstance )
		{
			// TODO Replace this with Value?
			this.left.assign( context, right );
			return right;
		}
		Assert.fail( "Unexpected " + this.right.getClass() );
		return null;
	}
}
