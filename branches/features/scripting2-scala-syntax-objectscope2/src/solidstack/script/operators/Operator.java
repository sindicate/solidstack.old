/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.script.operators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.expressions.Expression;
import solidstack.script.java.Types;
import solidstack.script.scopes.CombinedScope;
import solidstack.script.scopes.Scope;


abstract public class Operator implements Expression
{
	static private final HashMap<String, Integer> precedences;

	// TODO Make private
	protected String operator;
	protected Expression left;
	protected Expression right;

	/* Scala precedences: lowest to highest
		(all letters)
		|
		^
		&
		< >
		= !
		:
		+ -
		* / %
		(all other special characters)
	*/

	static
	{
		precedences = new HashMap<String, Integer>();

		precedences.put( "[", 1 ); // array index
		precedences.put( "(", 1 ); // method call
		precedences.put( ".", 1 ); // object member
//		precedences.put( "#", 1 ); // static member
		precedences.put( "new", 1 ); // object creation

//		precedences.put( "@++", 2 ); // postfix increment
//		precedences.put( "@--", 2 ); // postfix decrement
//
//		precedences.put( "++@", 3 ); // prefix increment
//		precedences.put( "--@", 3 ); // prefix decrement
		precedences.put( "+@", 3 ); // unary plus
		precedences.put( "-@", 3 ); // unary minus
//		precedences.put( "~", 3 ); // bitwise NOT
		precedences.put( "!@", 3 ); // boolean NOT
//		precedences.put( "(type)", 3 ); // type cast
		precedences.put( "as", 3 ); // type cast TODO Same precedence as instanceof?

		precedences.put( "*", 4 ); // multiplication
		precedences.put( "/", 4 ); // division
		precedences.put( "%", 4 ); // remainder

		precedences.put( "+", 5 ); // addition
		precedences.put( "-", 5 ); // subtraction

//		precedences.put( "<<", 6 ); // signed bit shift left
//		precedences.put( ">>", 6 ); // signed bit shift right
//		precedences.put( ">>>", 6 ); // unsigned bit shift right

		// In Groovy: .. and ... are between here

		precedences.put( "<", 7 ); // less than
		precedences.put( ">", 7 ); // greater than
		precedences.put( "<=", 7 ); // less than or equal
		precedences.put( ">=", 7 ); // greater than or equal
		precedences.put( "instanceof", 7 ); // reference test

		precedences.put( "==", 8 ); // equal to
		precedences.put( "!=", 8 ); // not equal to

//		precedences.put( "&", 9 ); // bitwise AND
//		precedences.put( "^", 10 ); // bitwise XOR
//		precedences.put( "|", 11 ); // bitwise OR
		precedences.put( "&&", 12 ); // boolean AND
		precedences.put( "||", 13 ); // boolean OR

//		precedences.put( "?", 14 ); // conditional

		precedences.put( "->", 15 ); // lambda TODO Equal to assignment precedence? Do we want that?
		precedences.put( "=>", 15 ); // lambda TODO Equal to assignment precedence? Do we want that?
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

		precedences.put( ",", 16 ); // tuple TODO Decide about this precedence
	}

	static public Operator operator( String name, Expression left, Expression right )
	{
		// TODO The ifs are not all necessary, for example * is always just *
		switch( name.charAt( 0 ) )
		{
			case '*':
				if( name.equals( "*" ) )
					return new Multiply( name, left, right );
				break;

			case '/':
				if( name.equals( "/" ) )
					return new Divide( name, left, right );
				break;

			case '%':
				if( name.equals( "%" ) )
					return new Remainder( name, left, right );
				break;

			case '+':
				if( name.equals( "+" ) )
					return new Plus( name, left, right );
				break;

			case '-':
				if( name.equals( "-" ) )
					return new Minus( name, left, right );
				if( name.equals( "->" ) )
					return new Associate( name, left, right );
				break;

			case '=':
				if( name.equals( "=" ) )
					return new Assign( name, left, right );
				if( name.equals( "==" ) )
					return new Equals( name, left, right );
				if( name.equals( "=>" ) )
					return new Function( name, left, right );
				break;

			case '!':
				if( name.equals( "!=" ) )
					return new NotEquals( name, left, right );
				break;

			case '<':
				if( name.equals( "<" ) )
					return new LessThan( name, left, right );
				if( name.equals( "<=" ) )
					return new LessOrEqualTo( name, left, right );
				break;

			case '>':
				if( name.equals( ">" ) )
					return new GreaterThan( name, left, right );
				if( name.equals( ">=" ) )
					return new GreaterOrEqualTo( name, left, right );
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

			case '[':
				if( name.equals( "[" ) )
					return new Index( name, left, right );
				break;

			case '.':
				if( name.equals( "." ) )
					return new Member( name, left, right );
				break;

//			case '#':
//				if( name.equals( "#" ) )
//					return new StaticMember( name, left, right );
//				break;

			case ':':
				if( name.equals( ":" ) )
					return new Associate( name, left, right );
				break;

			case ',':
				if( name.equals( "," ) )
					return new BuildTuple( ",", left, right );
				break;

			case 'a':
				if( name.equals( "as" ) )
					return new As( "as", left, right );
				break;

			case 'i':
				if( name.equals( "instanceof" ) )
					return new InstanceOf( "instanceof", left, right );
				break;
		}
		Assert.fail( "Unknown operator " + name );
		return null;
	}

	static public Operator preOp( SourceLocation location, String name, Expression right )
	{
		// TODO The ifs are not all necessary, for example * is always just *
		switch( name.charAt( 0 ) )
		{
			case '+':
				if( name.equals( "++@" ) )
					return new PreInc( location, name, right );
				break;

			case '-':
				if( name.equals( "-@" ) )
					return new Negate( location, name, right );
				if( name.equals( "--@" ) )
					return new PreDecr( location, name, right );
				break;

			case '!':
				if( name.equals( "!@" ) )
					return new Not( location, name, right );
				break;

			case 'n':
				if( name.equals( "new" ) )
					return new New( location, name, right );
				break;
		}
		Assert.fail( "Unknown operator " + name );
		return null;
	}

	public Expression getLeft()
	{
		return this.left;
	}

	public Expression getRight()
	{
		return this.right;
	}

//	public Object evaluateRef( ThreadContext thread )
//	{
//		return evaluate( thread );
//	}

	static protected Object add( Object left, Object right )
	{
		if( left instanceof String )
			return (String)left + right.toString(); // TODO In Java: whenever there is a string anywhere in the addition, everything becomes a string.

		if( left instanceof Scope )
		{
			Assert.isInstanceOf( right, Scope.class );
			return new CombinedScope( (Scope)left, (Scope)right );
		}

		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return ( (Number)left ).intValue() + ( (Number)right ).intValue();
			case 3:
				return ( (Number)left ).longValue() + ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)left ).add( (BigInteger)right );
			case 5:
				return ( (Number)left ).floatValue() + ( (Number)right ).floatValue();
			case 6:
				return ( (Number)left ).doubleValue() + ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)left ).add( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	static protected Object subtract( Object left, Object right )
	{
		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return ( (Number)left ).intValue() - ( (Number)right ).intValue();
			case 3:
				return ( (Number)left ).longValue() - ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)left ).subtract( (BigInteger)right );
			case 5:
				return ( (Number)left ).floatValue() - ( (Number)right ).floatValue();
			case 6:
				return ( (Number)left ).doubleValue() - ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)left ).subtract( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	static protected Object multiply( Object left, Object right )
	{
		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return ( (Number)left ).intValue() * ( (Number)right ).intValue();
			case 3:
				return ( (Number)left ).longValue() * ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)left ).multiply( (BigInteger)right );
			case 5:
				return ( (Number)left ).floatValue() * ( (Number)right ).floatValue();
			case 6:
				return ( (Number)left ).doubleValue() * ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)left ).multiply( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	static protected Object divide( Object left, Object right )
	{
		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return ( (Number)left ).intValue() / ( (Number)right ).intValue();
			case 3:
				return ( (Number)left ).longValue() / ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)left ).divide( (BigInteger)right );
			case 5:
				return ( (Number)left ).floatValue() / ( (Number)right ).floatValue();
			case 6:
				return ( (Number)left ).doubleValue() / ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)left ).divide( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	static protected Object remainder( Object left, Object right )
	{
		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return ( (Number)left ).intValue() % ( (Number)right ).intValue();
			case 3:
				return ( (Number)left ).longValue() % ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)left ).remainder( (BigInteger)right );
			case 5:
				return ( (Number)left ).floatValue() % ( (Number)right ).floatValue();
			case 6:
				return ( (Number)left ).doubleValue() % ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)left ).remainder( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	static public Object negate( Object right )
	{
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( right, right );
		int type = (Integer)operands[ 0 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return - ( (Number)right ).intValue();
			case 3:
				return - ( (Number)right ).longValue();
			case 4:
				return ( (BigInteger)right ).negate();
			case 5:
				return - ( (Number)right ).floatValue();
			case 6:
				return - ( (Number)right ).doubleValue();
			case 7:
				return ( (BigDecimal)right ).negate();
		}

		throw Assert.fail();
	}

	static public Object abs( Object right )
	{
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( right, right );
		int type = (Integer)operands[ 0 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return Math.abs( ( (Number)right ).intValue() );
			case 3:
				return Math.abs( ( (Number)right ).longValue() );
			case 4:
				return ( (BigInteger)right ).abs();
			case 5:
				return Math.abs( ( (Number)right ).floatValue() );
			case 6:
				return Math.abs( ( (Number)right ).doubleValue() );
			case 7:
				return ( (BigDecimal)right ).abs();
		}

		throw Assert.fail();
	}

	static protected int compare( Object left, Object right )
	{
		Assert.isTrue( left instanceof Number || left instanceof Character );
		Assert.isTrue( right instanceof Number || right instanceof Character );

		Object[] operands = Types.match( left, right );
		int type = (Integer)operands[ 0 ];
		left = operands[ 1 ];
		right = operands[ 2 ];
		switch( type )
		{
			case 2:
				return compareInt( ( (Number)left ).intValue(), ( (Number)right ).intValue() );
			case 3:
				return compareLong( ( (Number)left ).longValue(), ( (Number)right ).longValue() );
			case 4:
				return ( (BigInteger)left ).compareTo( (BigInteger)right );
			case 5:
				return Float.compare( ( (Number)left ).floatValue(), ( (Number)right ).floatValue() );
			case 6:
				return Double.compare( ( (Number)left ).doubleValue(), ( (Number)right ).doubleValue() );
			case 7:
				return ( (BigDecimal)left ).compareTo( (BigDecimal)right );
		}

		throw Assert.fail();
	}

	// Integer.compare() only exists in Java 7 and above
	static protected int compareInt( int int1, int int2 )
	{
		return int1 < int2 ? -1 : int1 == int2 ? 0 : 1;
	}

	// Long.compare() only exists in Java 7 and above
	static protected int compareLong( long int1, long int2 )
	{
		return int1 < int2 ? -1 : int1 == int2 ? 0 : 1;
	}

    protected Operator( String operator, Expression left, Expression right )
	{
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public Operator append( String operator, Expression expression )
	{
		Assert.isTrue( precedences.containsKey( operator ), "Unexpected operator " + operator );
		Assert.isTrue( precedences.containsKey( this.operator ), "Unexpected operator " + this.operator );

		int prec = precedences.get( operator );
		Assert.isTrue( prec > 0 );

		int myprec = precedences.get( this.operator );
		Assert.isTrue( myprec > 0 );

		// 14 (label) and 15 (assignment) go from right to left
		if( myprec < prec )
			return Operator.operator( operator, this, expression ); // this has precedence

		if( myprec == prec )
		{
			if( myprec < 14 )
				return Operator.operator( operator, this, expression ); // this has precedence
			if( myprec == 16 )
			{
				BuildTuple tuple = (BuildTuple)this;
				tuple.append( expression );
				return tuple;
			}
		}

		Expression last = getLast();
		// appended operator has precedence
		if( last instanceof Operator )
			setLast( ( (Operator)last ).append( operator, expression ) );
		else
			setLast( Operator.operator( operator, last, expression ) );
		return this;
	}

	protected Expression getLast()
	{
		return this.right;
	}

	protected void setLast( Expression expression )
	{
		this.right = expression;
	}

	public SourceLocation getLocation()
	{
		return this.left.getLocation();
	}

	public void writeTo( StringBuilder out )
	{
		if( this.left != null )
			this.left.writeTo( out );
		out.append( this.operator );
		if( this.right != null )
			this.right.writeTo( out );
	}
}
