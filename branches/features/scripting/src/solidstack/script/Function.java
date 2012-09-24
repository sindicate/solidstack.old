package solidstack.script;

import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.script.functions.Abs;
import solidstack.script.functions.Length;
import solidstack.script.functions.Println;
import solidstack.script.functions.Substr;
import solidstack.script.functions.Upper;


public class Function extends Expression
{
	private String name;
	protected List<Expression> parameters;

	static Function function( String name, List<Expression> parameters )
	{
		switch( name.charAt( 0 ) )
		{
			case 'a':
				if( name.equals( "abs" ) )
					return new Abs( name, parameters );
				break;
			case 'l':
				if( name.equals( "length" ) )
					return new Length( name, parameters );
				break;
			case 'p':
				if( name.equals( "println" ) )
					return new Println( name, parameters );
				break;
			case 's':
				if( name.equals( "substr" ) )
					return new Substr( name, parameters );
				break;
			case 'u':
				if( name.equals( "upper" ) )
					return new Upper( name, parameters );
				break;
		}
		Assert.fail( "Unknown function " + name );
		return null;
	}

	protected Function( String name, List<Expression> parameters )
	{
		this.name = name;
		this.parameters = parameters;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.fail( "Unknown function " + this.name );
		return null;
	}
}
