package solidstack.script.functions;

import java.math.BigDecimal;
import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.FunctionInstance;

public class Abs extends FunctionInstance
{
	public Abs()
	{
		super( null, null );
	}

	@Override
	public Object call( Context context, List<Object> parameters )
	{
		Assert.isTrue( parameters.size() == 1 );
		Object object = parameters.get( 0 );
		Assert.isInstanceOf( object, BigDecimal.class );
		return ( (BigDecimal)object ).abs();
	}
}
