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

package solidstack.script;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import solidstack.io.SourceLocation;
import solidstack.lang.Assert;
import solidstack.script.AbstractContext.Undefined;
import solidstack.script.AbstractContext.Value;
import solidstack.script.operations.Access;
import solidstack.script.operations.And;
import solidstack.script.operations.Apply;
import solidstack.script.operations.Assign;
import solidstack.script.operations.Equals;
import solidstack.script.operations.GreaterThan;
import solidstack.script.operations.LessThan;
import solidstack.script.operations.Minus;
import solidstack.script.operations.Multiply;
import solidstack.script.operations.Negate;
import solidstack.script.operations.Not;
import solidstack.script.operations.NotEquals;
import solidstack.script.operations.Or;
import solidstack.script.operations.Plus;
import solidstack.script.operations.PostDecr;
import solidstack.script.operations.PostInc;
import solidstack.script.operations.PreDecr;
import solidstack.script.operations.PreInc;


abstract public class Operation implements Expression
{
	static private final HashMap<String, Integer> precedences;

	// TODO Make private
	protected String operation;
	protected Expression left;
	protected Expression middle;
	protected Expression right;

	static
	{
		precedences = new HashMap<String, Integer>();

//		precedences.put( "[", 1 ); // array index
		precedences.put( "(", 1 ); // method call
		precedences.put( ".", 1 ); // member access

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

		precedences.put( "->", 15 ); // lambda TODO Equal to assignment precedence? Do we want that?

		precedences.put( "=", 15 ); // assignment
//		precedences.put( "*=", 16 ); // assignment
//		precedences.put( "/=", 16 ); // assignment
//		precedences.put( "+=", 16 ); // assignment
//		precedences.put( "-=", 16 ); // assignment
//		precedences.put( "%=", 16 ); // assignment
//		precedences.put( "<<=", 16 ); // assignment
//		precedences.put( ">>=", 16 ); // assignment
//		precedences.put( ">>>=", 16 ); // assignment
//		precedences.put( "&=", 16 ); // assignment
//		precedences.put( "^=", 16 ); // assignment
//		precedences.put( "|=", 16 ); // assignment
	}

	static Operation operation( String name, Expression left, Expression right )
	{
		// TODO The ifs are not all necessary, for example * is always just *
		switch( name.charAt( 0 ) )
		{
			case '*':
				if( name.equals( "*" ) )
					return new Multiply( name, left, right );
				break;

			case '+':
				if( name.equals( "+" ) )
					return new Plus( name, left, right );
				break;

			case '-':
				if( name.equals( "-" ) )
					return new Minus( name, left, right );
				break;

			case '=':
				if( name.equals( "=" ) )
					return new Assign( name, left, right );
				if( name.equals( "==" ) )
					return new Equals( name, left, right );
				break;

			case '!':
				if( name.equals( "!=" ) )
					return new NotEquals( name, left, right );
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

			case '.':
				if( name.equals( "." ) )
					return new Access( name, left, right );
				break;
		}
		Assert.fail( "Unknown operation " + name );
		return null;
	}

	static Operation preOp( SourceLocation location, String name, Expression right )
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
		}
		Assert.fail( "Unknown operation " + name );
		return null;
	}

	static public Object unwrap( Object object )
	{
		if( object instanceof Value )
			return ( (Value)object ).get(); // TODO These may be too late. Maybe we should bind the Value reference and the actual value.
		return object;
	}

	static public Object unwrapList( List<Object> objects )
	{
		// TODO These may be too late. Maybe we should bind the Value reference and the actual value.
		for( ListIterator<Object> i = objects.listIterator(); i.hasNext(); )
		{
			Object object = i.next();
			if( object instanceof Value )
				i.set( ( (Value)object ).get() );
		}
		return objects;
	}

	static public Object evaluateAndUnwrap( Expression expression, ThreadContext thread )
	{
		Object result = expression.evaluate( thread );
		if( result instanceof TupleValue )
		{
			TupleValue results = (TupleValue)result;
			if( results.size() == 0 )
				return null;
			result = results.getLast();
		}
		if( result instanceof Value ) // TODO Does this ever happen with tuples?
			return ( (Value)result ).get();
		return result;
	}

	static protected Object add( Object left, Object right )
	{
		// TODO Type conversions
		if( left instanceof BigDecimal )
		{
			Assert.isInstanceOf( right, BigDecimal.class );
			return ( (BigDecimal)left ).add( (BigDecimal)right );
		}
		if( left instanceof Integer )
		{
			Assert.isInstanceOf( right, BigDecimal.class );
			left = new BigDecimal( (Integer)left );
			return ( (BigDecimal)left ).add( (BigDecimal)right );
		}
		if( left instanceof AbstractContext )
		{
			Assert.isInstanceOf( right, AbstractContext.class );
			return new CombinedContext( (AbstractContext)left, (AbstractContext)right );
		}
		Assert.isInstanceOf( left, String.class, "Not expecting " + left.getClass() );
		if( !( right instanceof String ) )
			right = right.toString();
		return (String)left + (String)right;
	}

	static protected Object minus( Object left, Object right )
	{
		Assert.isInstanceOf( left, BigDecimal.class );
		Assert.isInstanceOf( right, BigDecimal.class );
		return ( (BigDecimal)left ).subtract( (BigDecimal)right );
	}

	static public Object negate( Object object )
	{
		Assert.isInstanceOf( object, BigDecimal.class );
		return ( (BigDecimal)object ).negate();
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
		if( left instanceof Boolean )
			return (Boolean)left;
		if( left instanceof String )
			return ( (String)left ).length() != 0;
		if( left instanceof SuperString )
			return !( (SuperString)left ).isEmpty();
		return left != null && left != Null.INSTANCE && !( left instanceof Undefined );
	}

	protected Operation( String operation, Expression left, Expression right )
	{
		this.operation = operation;
		this.left = left;
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

		// 14: ?:, 15: = and 16: -> go from right to left
		if( myprec < prec || myprec == prec && myprec < 14 )
			return Operation.operation( operation, this, expression ); // this has precedence

		// appended operation has precedence
		if( this.right instanceof Operation )
			this.right = ( (Operation)this.right ).append( operation, expression );
		else
			this.right = Operation.operation( operation, this.right, expression );
		return this;
	}

	public SourceLocation getLocation()
	{
		return this.left.getLocation();
	}
}
