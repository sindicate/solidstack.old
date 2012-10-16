package solidstack.script.operations;


import org.springframework.util.Assert;

import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.Identifier;
import solidstack.script.ObjectAccess;
import solidstack.script.Operation;


public class Access extends Operation
{
	public Access( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		Assert.isInstanceOf( Identifier.class, this.right );
		String right = ( (Identifier)this.right ).getName();
		return new ObjectAccess( left, right );
	}
}
