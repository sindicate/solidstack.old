package solidstack.script.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Function;

public class Abs extends Function
{
	public Abs( String name, List<Expression> parameters )
	{
		super( name, parameters );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isTrue( this.parameters.size() == 1 );
		Object object = this.parameters.get( 0 ).evaluate( context );
		Assert.isInstanceOf( object, BigDecimal.class );
		return ( (BigDecimal)object ).abs();
	}
}
