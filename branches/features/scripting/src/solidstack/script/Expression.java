package solidstack.script;

import java.util.List;

import solidstack.lang.Assert;

abstract public class Expression
{
	abstract public Object evaluate( Context context );

	public Object assign( Context context, Object value )
	{
		Assert.fail( "Can't assign to " + getClass().getName() );
		return value;
	}

	public Expression append( List<Expression> parameters )
	{
		Assert.fail( "Can't add parameters to " + getClass().getName() );
		return null;
	}
}
