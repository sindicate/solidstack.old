package solidstack.script.functions;

import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Function;

public class Upper extends Function
{
	public Upper( String name, List<Expression> parameters )
	{
		super( name, parameters );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isTrue( this.parameters.size() == 1 );
		Object object = this.parameters.get( 0 ).evaluate( context );
		Assert.isInstanceOf( object, String.class );
		return ( (String)object ).toUpperCase();
	}
}
