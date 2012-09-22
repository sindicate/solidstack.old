package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;


public class Operation extends Expression
{
	private String operation;
	private Expression left;
	private Expression right;

	public Operation( String operation, Expression left, Expression right )
	{
		this.operation = operation;
		this.left = left;
		this.right = right;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object left = this.left.evaluate( context );
		Object right = this.right.evaluate( context );

		if( this.operation.equals( "+" ) )
		{
			Assert.isInstanceOf( left, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).add( (BigDecimal)right );
		}

		if( this.operation.equals( "*" ) )
		{
			Assert.isInstanceOf( left, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).multiply( (BigDecimal)right );
		}

		Assert.fail( "Unknown operation " + this.operation );
		return null;
	}

	public Operation append( String operation, Expression right )
	{
		if( this.operation.equals( "+" ) )
			if( operation.equals( "*" ) )
			{
				this.right = new Operation( operation, this.right, right );
				return this;
			}

		return new Operation( operation, this, right );
	}
}
