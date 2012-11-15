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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import solidstack.io.ReaderSourceReader;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.ClassMember;
import solidstack.script.objects.FunctionObject.ParWalker;
import solidstack.script.objects.FunnyString;
import solidstack.script.objects.Null;
import solidstack.script.objects.ObjectMember;
import solidstack.script.objects.Tuple;
import solidstack.script.scopes.AbstractScope.Undefined;
import solidstack.script.scopes.AbstractScope.Value;
import solidstack.script.scopes.Scope;

public class Script
{
	private Expression expression;

	public Script( Expression expression )
	{
		this.expression = expression;
	}

	// TODO Add location
	static public Script compile( String script )
	{
		ScriptTokenizer t = new ScriptTokenizer( new ReaderSourceReader( new StringReader( script ) ) );
		ScriptParser p = new ScriptParser( t );
		Expression result = p.parse();
		return new Script( result );
	}

	public Object execute( Scope scope )
	{
		if( this.expression == null )
			return null;

		if( scope == null )
			scope = new Scope();

		ThreadContext thread = ThreadContext.init( scope );
		return toJava( this.expression.evaluate( thread ) );
	}

	static public Object single( Object value )
	{
		if( value instanceof Tuple )
		{
			Tuple results = (Tuple)value;
			if( results.size() == 0 )
				return Null.INSTANCE;
			value = results.getLast();
		}
		if( value instanceof Value ) // TODO Does this ever happen with tuples?
			return ( (Value)value ).get();
		return value;
	}

	static public Object deref( Object value )
	{
		if( value instanceof List )
		{
			// TODO Or create a new list?
			List<Object> list = (List<Object>)value;
			for( ListIterator<Object> i = list.listIterator(); i.hasNext(); )
				i.set( deref( i.next() ) );
			return list;
		}
		if( value instanceof Value )
			return ( (Value)value ).get();
		return value;
	}

	static public Object toJava( Object value )
	{
		Object result = single( value );
		if( result == Null.INSTANCE )
			return null;
		if( result instanceof FunnyString )
			return result.toString();
		if( result instanceof ObjectMember )
			return ( (ObjectMember)result ).get();
		if( result instanceof ClassMember )
			return ( (ClassMember)result ).get();
		return result;
	}

	static public Object[] toJavaParameters( Object[] values )
	{
		List<Object> result = new ArrayList<Object>();
		ParWalker pw = new ParWalker( values );
		Object par = pw.get();
		while( par != null )
		{
			result.add( toJava( par ) );
			par = pw.get();
		}
		return result.toArray( new Object[ result.size() ] );
	}

	static public Object toScript( Object value )
	{
		if( value == null )
			return Null.INSTANCE;
		return value;
	}

	static public final Object[] EMPTY_ARRAY = new Object[ 0 ];

	static public Object[] toArray( Object values )
	{
		Object[] result;
		if( values instanceof Tuple )
			return ( (Tuple)values ).list().toArray();
		if( values != null )
			return new Object[] { values };
		return EMPTY_ARRAY;
	}

	static public boolean isTrue( Object left )
	{
		if( left instanceof Boolean )
			return (Boolean)left;
		if( left instanceof String )
			return ( (String)left ).length() != 0;
		if( left instanceof FunnyString )
			return !( (FunnyString)left ).isEmpty();
		return left != null && left != Null.INSTANCE && !( left instanceof Undefined );
	}
}
