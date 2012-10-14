package solidstack.script.functions;

import java.math.BigDecimal;
import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.FunctionInstance;

public class Substr extends FunctionInstance
{
	public Substr()
	{
		super( null, null );
	}

	@Override
	public Object call( Context context, List<Object> parameters )
	{
		Object object = parameters.get( 0 );
		Object start = parameters.get( 1 );
		Assert.isInstanceOf( object, String.class );
		Assert.isInstanceOf( start, BigDecimal.class );
		if( parameters.size() == 2 )
			return ( (String)object ).substring( ( (BigDecimal)start ).intValue() );
		Assert.isTrue( parameters.size() == 3 );
		Object end = parameters.get( 2 );
		Assert.isInstanceOf( end, BigDecimal.class );
		return ( (String)object ).substring( ( (BigDecimal)start ).intValue(), ( (BigDecimal)end ).intValue() );
	}
}
