package solidstack.script.functions;

import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.Context;
import solidstack.script.FunctionInstance;

public class Println extends FunctionInstance
{
	public Println()
	{
		super( null, null );
	}

	@Override
	public Object call( Context context, List<?> parameters )
	{
		Assert.isTrue( parameters.size() == 1 );
		Object object = parameters.get( 0 );
		System.out.println( object );
		return object;
	}
}
