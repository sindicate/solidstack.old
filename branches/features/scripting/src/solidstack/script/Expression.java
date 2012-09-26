package solidstack.script;

import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;

abstract public class Expression
{
	abstract public Object evaluate( Map<String, Object> context );

	public Object assign( Map<String, Object> context, Object value )
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
