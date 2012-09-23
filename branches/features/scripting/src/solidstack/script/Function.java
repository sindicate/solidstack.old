package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;


public class Function extends Expression
{
	private String name;
	private Expression parameter;

	public Function( String name, Expression parameter )
	{
		this.name = name;
		this.parameter = parameter;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		if( this.name.equals( "abs" ) )
		{
			Object object = this.parameter.evaluate( context );
			Assert.isInstanceOf( object, BigDecimal.class );
			return ( (BigDecimal)object ).abs();
		}
		Assert.fail( "Unknown function " + this.name );
		return null;
	}
}
