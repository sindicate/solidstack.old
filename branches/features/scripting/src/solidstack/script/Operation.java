package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

import solidstack.lang.Assert;


public class Operation extends Expression
{
	private String operation;
	private Expression left;
	private Expression right;
	private Expression right2;

	public Operation( String operation, Expression left, Expression right )
	{
		this.operation = operation;
		this.left = left;
		this.right = right;
	}

	public Operation( Expression left, Expression right, Expression right2 )
	{
		this.operation = "?";
		this.left = left;
		this.right = right;
		this.right2 = right2;
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

		if( this.operation.equals( "?" ) )
		{
			Object right2 = this.right2.evaluate( context );
			Assert.isInstanceOf( left, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			Assert.isInstanceOf( right2, BigDecimal.class );
			if( ( (BigDecimal)left ).compareTo( new BigDecimal( 0 ) ) != 0 )
				return right;
			return right2;
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

		if( this.operation.equals( "?" ) )
		{
			this.right2 = new Operation( operation, this.right2, right );
			return this;
		}

		return new Operation( operation, this, right );
	}

	public Expression append( String operation, Expression first, Expression second )
	{
		return new Operation( this, first, second );
	}
}
