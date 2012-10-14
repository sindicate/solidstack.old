package solidstack.script;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Operation extends Expression
{
	static protected final HashMap<String, Integer> precedences;

	private String operation;
	private Expression left;
	private Expression middle;
	private Expression right;

	static
	{
		precedences = new HashMap<String, Integer>();

//		precedences.put( "[", 1 ); // array index
//		precedences.put( "(", 1 ); // method call
//		precedences.put( ".", 1 ); // member access

		precedences.put( "@++", 2 ); // postfix increment
		precedences.put( "@--", 2 ); // postfix decrement

		precedences.put( "++@", 3 ); // prefix increment
		precedences.put( "--@", 3 ); // prefix decrement
		precedences.put( "+@", 3 ); // unary plus
		precedences.put( "-@", 3 ); // unary minus
//		precedences.put( "~", 3 ); // bitwise NOT
		precedences.put( "!@", 3 ); // boolean NOT
//		precedences.put( "(type)", 3 ); // type cast
////		precedences.put( "new", 3 ); // object creation

		precedences.put( "*", 4 ); // multiplication
		precedences.put( "/", 4 ); // division
		precedences.put( "%", 4 ); // remainder

		precedences.put( "+", 5 ); // addition
		precedences.put( "-", 5 ); // subtraction

//		precedences.put( "<<", 6 ); // signed bit shift left
//		precedences.put( ">>", 6 ); // signed bit shift right
//		precedences.put( ">>>", 6 ); // unsigned bit shift right

		precedences.put( "<", 7 ); // less than
		precedences.put( ">", 7 ); // greater than
//		precedences.put( "<=", 7 ); // less than or equal
//		precedences.put( ">=", 7 ); // greater than or equal
//		precedences.put( "instanceof", 7 ); // reference test
//
		precedences.put( "==", 8 ); // equal to
//		precedences.put( "!=", 8 ); // not equal to
//
//		precedences.put( "&", 9 ); // bitwise AND
//
//		precedences.put( "^", 10 ); // bitwise XOR
//
//		precedences.put( "|", 11 ); // bitwise OR

		precedences.put( "&&", 12 ); // boolean AND

		precedences.put( "||", 13 ); // boolean OR

		precedences.put( "?", 14 ); // conditional
		precedences.put( ":", 14 ); // conditional

		precedences.put( "=", 15 ); // assignment
//		precedences.put( "*=", 15 ); // assignment
//		precedences.put( "/=", 15 ); // assignment
//		precedences.put( "+=", 15 ); // assignment
//		precedences.put( "-=", 15 ); // assignment
//		precedences.put( "%=", 15 ); // assignment
//		precedences.put( "<<=", 15 ); // assignment
//		precedences.put( ">>=", 15 ); // assignment
//		precedences.put( ">>>=", 15 ); // assignment
//		precedences.put( "&=", 15 ); // assignment
//		precedences.put( "^=", 15 ); // assignment
//		precedences.put( "|=", 15 ); // assignment
	}

	public Operation( String operation, Expression left, Expression right )
	{
		this.operation = operation;
		this.left = left;
		this.right = right;
	}

	public Operation( Expression left, Expression middle, Expression right )
	{
		this.operation = "?";
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		if( this.operation.equals( "@++" ) )
		{
			Object left = this.left.evaluate( context );
			if( !( left instanceof Value ) )
				throw new ScriptException( "Tried to apply ++ to a non mutable value " + left.getClass().getName() );
			Value value = (Value)left;
			Object result = value.get();
			value.set( add( result, new BigDecimal( 1 ) ) );
			return result;
		}

		if( this.operation.equals( "++@" ) )
		{
			Object right = this.right.evaluate( context );
			if( !( right instanceof Value ) )
				throw new ScriptException( "Tried to apply ++ to a non mutable value " + right.getClass().getName() );
			Value value = (Value)right;
			Object result = add( value.get(), new BigDecimal( 1 ) );
			value.set( result );
			return result;
		}

		if( this.operation.equals( "&&" ) )
		{
			Object left = this.left.evaluate( context );
			Assert.isInstanceOf( left, Boolean.class );
			if( !(Boolean)left )
				return false;
			Object right = this.right.evaluate( context );
			Assert.isInstanceOf( right, Boolean.class );
			return right;
		}

		if( this.operation.equals( "||" ) )
		{
			Object left = this.left.evaluate( context );
			Assert.isInstanceOf( left, Boolean.class );
			if( (Boolean)left )
				return true;
			Object right = this.right.evaluate( context );
			Assert.isInstanceOf( right, Boolean.class );
			return right;
		}

		Object left = null;
		if( this.left != null )
		{
			left = this.left.evaluate( context );
			if( left instanceof Value )
				left = ( (Value)left ).get();
		}

		Object right = this.right.evaluate( context );
		if( right instanceof Value )
			right = ( (Value)right ).get();

		if( this.operation.equals( "!@" ) )
		{
			if( right instanceof Boolean )
				return !(Boolean)right;
			if( right instanceof BigDecimal )
				return ( (BigDecimal)right ).compareTo( new BigDecimal( 0 ) ) == 0;
			if( right != null )
				throw new ScriptException( "Tried to apply ! to a " + right.getClass().getName() );
			throw new ScriptException( "Tried to apply ! to null" );
		}

		if( this.operation.equals( "=" ) )
		{
			Assert.isInstanceOf( right, BigDecimal.class );
			return this.left.assign( context, right );
		}

		if( this.operation.equals( "==" ) )
		{
			if( left == null )
				return right == null;
			return left.equals( right );
		}

		if( this.operation.equals( "<" ) )
		{
			return compare( left, right ) < 0;
		}

		if( this.operation.equals( ">" ) )
		{
			return compare( left, right ) > 0;
		}

		if( this.operation.equals( "+" ) )
		{
			return add( left, right );
		}

		if( this.operation.equals( "-" ) )
		{
			Assert.isInstanceOf( left, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).subtract( (BigDecimal)right );
		}

		if( this.operation.equals( "*" ) )
		{
			Assert.isInstanceOf( left, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).multiply( (BigDecimal)right );
		}

		if( this.operation.equals( "?" ) )
		{
			Object middle = this.middle.evaluate( context );
			Assert.isInstanceOf( middle, BigDecimal.class );
			Assert.isInstanceOf( right, BigDecimal.class );
			if( isTrue( left ) )
				return middle;
			return right;
		}

		Assert.fail( "Unknown operation " + this.operation );
		return null;
	}

	static Object add( Object left, Object right )
	{
		if( left instanceof BigDecimal )
		{
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).add( (BigDecimal)right );
		}
		Assert.isInstanceOf( left, String.class );
		Assert.isInstanceOf( right, String.class );
		return (String)left + (String)right;
	}

	static int compare( Object left, Object right )
	{
		if( left instanceof BigDecimal )
		{
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).compareTo( (BigDecimal)right );
		}
		Assert.isInstanceOf( left, String.class );
		Assert.isInstanceOf( right, String.class );
		return ( (String)left ).compareTo( (String)right );
	}

	static boolean isTrue( Object left )
	{
		if( left instanceof BigDecimal )
			return ( (BigDecimal)left ).compareTo( new BigDecimal( 0 ) ) != 0;
		Assert.isInstanceOf( left, Boolean.class );
		return (Boolean)left;
	}

	public Operation append( String operation, Expression expression )
	{
		Assert.isTrue( precedences.containsKey( operation ), "Unexpected operation " + operation );
		Assert.isTrue( precedences.containsKey( this.operation ), "Unexpected operation " + this.operation );

		int prec = precedences.get( operation );
		Assert.isTrue( prec > 0 );

		int myprec = precedences.get( this.operation );
		Assert.isTrue( myprec > 0 );

		if( myprec < prec || myprec == prec && myprec < 14 )
			return new Operation( operation, this, expression );

		if( this.right instanceof Operation )
			this.right = ( (Operation)this.right ).append( operation, expression );
		else
			this.right = new Operation( operation, this.right, expression );
		return this;
	}

	// Append ?
	public Expression append( Expression first, Expression second )
	{
		Assert.isTrue( precedences.containsKey( this.operation ), "Unexpected operation " + this.operation );

		int prec = precedences.get( "?" );
		Assert.isTrue( prec > 0 );

		int myprec = precedences.get( this.operation );
		Assert.isTrue( myprec > 0 );

		if( myprec < prec )
			return new Operation( this, first, second );

		 // Only happens when appending ? to = or another ?
		if( this.right instanceof Operation )
			this.right = ( (Operation)this.right ).append( first, second );
		else
			this.right = new Operation( this.right, first, second );
		return this;
	}

	@Override
	public Expression append( List<Expression> parameters )
	{
		this.right = this.right.append( parameters );
		return this;
	}
}
