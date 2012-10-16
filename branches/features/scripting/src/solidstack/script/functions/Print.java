package solidstack.script.functions;

import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.FunctionInstance;

public class Print extends FunctionInstance
{
	public Print()
	{
		super( null, null );
	}

	@Override
	public Object call( Context context, List<?> parameters )
	{
		Assert.isTrue( parameters.size() == 1 );
		Object object = parameters.get( 0 );
		System.out.print( object );
		return object;
	}
}
