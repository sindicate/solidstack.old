package solidstack.script;

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
}
