package solidstack.script.operations;

import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Operation;


public class Or extends Operation
{
	public Or( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		Assert.isInstanceOf( left, Boolean.class );
		if( (Boolean)left )
			return true;

		Object right = evaluateAndUnwrap( this.right, context );
		Assert.isInstanceOf( right, Boolean.class );
		return right;
	}
}
