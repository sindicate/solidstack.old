package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;


public class Identifier extends Expression
{
	private String name;

	public Identifier( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object result = context.get( this.name );
		if( result instanceof BigDecimal || result instanceof String )
			return result;
		if( result instanceof Integer )
			return new BigDecimal( (Integer)result );
		Assert.fail( "Unexpected type " + result.getClass().getName() );
		return null;
	}

	@Override
	public Object assign( Map<String, Object> context, Object value )
	{
		context.put( this.name, value );
		return value;
	}
}
