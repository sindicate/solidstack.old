package solidstack.script.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Function;

public class Substr extends Function
{
	public Substr( String name, List<Expression> parameters )
	{
		super( name, parameters );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object object = this.parameters.get( 0 ).evaluate( context );
		Object start = this.parameters.get( 1 ).evaluate( context );
		Assert.isInstanceOf( object, String.class );
		Assert.isInstanceOf( start, BigDecimal.class );
		if( this.parameters.size() == 2 )
			return ( (String)object ).substring( ( (BigDecimal)start ).intValue() );
		Assert.isTrue( this.parameters.size() == 3 );
		Object end = this.parameters.get( 2 ).evaluate( context );
		Assert.isInstanceOf( end, BigDecimal.class );
		return ( (String)object ).substring( ( (BigDecimal)start ).intValue(), ( (BigDecimal)end ).intValue() );
	}
}
