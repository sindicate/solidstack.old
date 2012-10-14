package solidstack.script;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import solidstack.lang.Assert;
import solidstack.script.operations.And;
import solidstack.script.operations.Apply;
import solidstack.script.operations.Assign;
import solidstack.script.operations.Equals;
import solidstack.script.operations.GreaterThan;
import solidstack.script.operations.IfExp;
import solidstack.script.operations.LessThan;
import solidstack.script.operations.Minus;
import solidstack.script.operations.Multiply;
import solidstack.script.operations.Not;
import solidstack.script.operations.Or;
import solidstack.script.operations.Plus;
import solidstack.script.operations.PostDecr;
import solidstack.script.operations.PostInc;
import solidstack.script.operations.PreDecr;
import solidstack.script.operations.PreInc;


abstract public class Operation extends Expression
{
	static protected final HashMap<String, Integer> precedences;

	protected String operation;
	protected Expression left;
	protected Expression middle;
	protected Expression right;

	static
	{
		precedences = new HashMap<String, Integer>();

//		precedences.put( "[", 1 ); // array index
		precedences.put( "(", 1 ); // method call
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

	static Operation operation( String name, Expression left, Expression right )
	{
		switch( name.charAt( 0 ) )
		{
			case '*':
				if( name.equals( "*" ) )
					return new Multiply( name, left, right );
				break;

			case '+':
				if( name.equals( "+" ) )
					return new Plus( name, left, right );
				if( name.equals( "++@" ) )
					return new PreInc( name, left, right );
				break;

			case '-':
				if( name.equals( "-" ) )
					return new Minus( name, left, right );
				if( name.equals( "--@" ) )
					return new PreDecr( name, left, right );
				break;

			case '=':
				if( name.equals( "=" ) )
					return new Assign( name, left, right );
				if( name.equals( "==" ) )
					return new Equals( name, left, right );
				break;

			case '!':
				if( name.equals( "!@" ) )
					return new Not( name, left, right );
				break;

			case '<':
				if( name.equals( "<" ) )
					return new LessThan( name, left, right );
				break;

			case '>':
				if( name.equals( ">" ) )
					return new GreaterThan( name, left, right );
				break;

			case '@':
				if( name.equals( "@++" ) )
					return new PostInc( name, left, right );
				if( name.equals( "@--" ) )
					return new PostDecr( name, left, right );
				break;

			case '&':
				if( name.equals( "&&" ) )
					return new And( name, left, right );
				break;

			case '|':
				if( name.equals( "||" ) )
					return new Or( name, left, right );
				break;

			case '(':
				if( name.equals( "(" ) )
					return new Apply( name, left, right );
				break;
		}
		Assert.fail( "Unknown operation " + name );
		return null;
	}

	static Operation operation( Expression left, Expression middle, Expression right )
	{
		return new IfExp( left, middle, right );
	}

	static protected Object evaluateAndUnwrap( Expression expression, Context context )
	{
		Object result = expression.evaluate( context );
		if( result instanceof Value )
			return ( (Value)result ).get();
		return result;
	}

	static protected Object add( Object left, Object right )
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

	static protected Object minus( Object left, Object right )
	{
		Assert.isInstanceOf( left, BigDecimal.class );
		Assert.isInstanceOf( right, BigDecimal.class );
		return ( (BigDecimal)left ).subtract( (BigDecimal)right );
	}

	static protected int compare( Object left, Object right )
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

	static protected boolean isTrue( Object left )
	{
		if( left instanceof BigDecimal )
			return ( (BigDecimal)left ).compareTo( new BigDecimal( 0 ) ) != 0;
		Assert.isInstanceOf( left, Boolean.class );
		return (Boolean)left;
	}

	protected Operation( String operation, Expression left, Expression right )
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

	public Operation append( String operation, Expression expression )
	{
		Assert.isTrue( precedences.containsKey( operation ), "Unexpected operation " + operation );
		Assert.isTrue( precedences.containsKey( this.operation ), "Unexpected operation " + this.operation );

		int prec = precedences.get( operation );
		Assert.isTrue( prec > 0 );

		int myprec = precedences.get( this.operation );
		Assert.isTrue( myprec > 0 );

		if( myprec < prec || myprec == prec && myprec < 14 )
			return Operation.operation( operation, this, expression );

		if( this.right instanceof Operation )
			this.right = ( (Operation)this.right ).append( operation, expression );
		else
			this.right = Operation.operation( operation, this.right, expression );
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
			return Operation.operation( this, first, second );

		 // Only happens when appending ? to = or another ?
		if( this.right instanceof Operation )
			this.right = ( (Operation)this.right ).append( first, second );
		else
			this.right = Operation.operation( this.right, first, second );
		return this;
	}

	@Override
	public Expression append( List<Expression> parameters )
	{
		this.right = this.right.append( parameters );
		return this;
	}
}
