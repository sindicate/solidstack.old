package solidstack.script.functions;

import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.Expression;
import solidstack.script.Function;

public class Println extends Function
{
	public Println( String name, List<Expression> parameters )
	{
		super( name, parameters );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isTrue( this.parameters.size() == 1 );
		Object object = this.parameters.get( 0 ).evaluate( context );
		if( object instanceof String )
		{
			String print = (String)object;
			System.out.println( print );
			return print;
		}
		Assert.isInstanceOf( object, Boolean.class );
		boolean print = (Boolean)object;
		System.out.println( print );
		return print;
	}
}
