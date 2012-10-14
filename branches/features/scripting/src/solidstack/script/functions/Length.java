package solidstack.script.functions;

import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.FunctionInstance;

public class Length extends FunctionInstance
{
	public Length()
	{
		super( null, null );
	}

	@Override
	public Object call( Context context, List<Object> parameters )
	{
		Assert.isTrue( parameters.size() == 1 );
		Object object = parameters.get( 0 );
		Assert.isInstanceOf( object, String.class );
		return ( (String)object ).length();
	}
}
