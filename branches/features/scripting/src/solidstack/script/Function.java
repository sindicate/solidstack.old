package solidstack.script;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Function extends Expression
{
	private String name;
	private List<Expression> parameters;

	public Function( String name, List<Expression> parameters )
	{
		this.name = name;
		this.parameters = parameters;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		if( this.name.equals( "abs" ) )
		{
			Assert.isTrue( this.parameters.size() == 1 );
			Object object = this.parameters.get( 0 ).evaluate( context );
			Assert.isInstanceOf( object, BigDecimal.class );
			return ( (BigDecimal)object ).abs();
		}
		if( this.name.equals( "substr" ) )
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
		if( this.name.equals( "upper" ) )
		{
			Assert.isTrue( this.parameters.size() == 1 );
			Object object = this.parameters.get( 0 ).evaluate( context );
			Assert.isInstanceOf( object, String.class );
			return ( (String)object ).toUpperCase();
		}
		if( this.name.equals( "println" ) )
		{
			Assert.isTrue( this.parameters.size() == 1 );
			Object object = this.parameters.get( 0 ).evaluate( context );
			Assert.isInstanceOf( object, String.class );
			String print = (String)object;
			System.out.println( print );
			return print;
		}
		if( this.name.equals( "length" ) )
		{
			Assert.isTrue( this.parameters.size() == 1 );
			Object object = this.parameters.get( 0 ).evaluate( context );
			Assert.isInstanceOf( object, String.class );
			return ( (String)object ).length();
		}
		Assert.fail( "Unknown function " + this.name );
		return null;
	}
}
