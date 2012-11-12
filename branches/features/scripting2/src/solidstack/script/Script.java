/*--
 * Copyright 2012 Ren� M. de Bloois
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
import java.util.List;
import java.util.ListIterator;

import solidstack.io.ReaderSourceReader;
import solidstack.script.context.AbstractContext.Value;
import solidstack.script.context.Context;
import solidstack.script.expressions.Expression;
import solidstack.script.objects.ClassAccess;
import solidstack.script.objects.Null;
import solidstack.script.objects.ObjectAccess;
import solidstack.script.objects.SuperString;
import solidstack.script.objects.TupleValue;

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

	public Object execute( Context context )
	{
		if( this.expression == null )
			return null;

		if( context == null )
			context = new Context();

		ThreadContext thread = ThreadContext.init( context );
		return toJava( this.expression.evaluate( thread ) );
	}

	static public Object single( Object value )
	{
		if( value instanceof TupleValue )
		{
			TupleValue results = (TupleValue)value;
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
		if( result instanceof SuperString )
			return result.toString();
		if( result instanceof ObjectAccess )
			return ( (ObjectAccess)result ).get();
		if( result instanceof ClassAccess )
			return ( (ClassAccess)result ).get();
		return result;
	}

	static public Object[] toJavaParameters( Object values )
	{
		Object[] result = toArray( values );
		int count = result.length;
		for( int i = 0; i < count; i++ )
			result[ i ] = toJava( result[ i ] );
		return result;
	}

	static public Object toScript( Object value )
	{
		if( value == null )
			return Null.INSTANCE;
		return value;
	}

	static public Object[] toScriptParameters( Object values )
	{
		Object[] result = toArray( values );
		int count = result.length;
		for( int i = 0; i < count; i++ )
			result[ i ] = result[ i ];
		return result;
	}

	static public final Object[] EMPTY_ARRAY = new Object[ 0 ];

	static public Object[] toArray( Object values )
	{
		Object[] result;
		if( values instanceof TupleValue )
			return ( (TupleValue)values ).getValues().toArray();
		if( values != null )
			return new Object[] { values };
		return EMPTY_ARRAY;
	}
}
