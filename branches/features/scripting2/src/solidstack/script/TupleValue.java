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

import java.util.ArrayList;
import java.util.List;

import solidstack.script.Context.Value;

public class TupleValue
{
	private List<Object> values = new ArrayList<Object>();

	public TupleValue()
	{
		// TODO Auto-generated constructor stub
	}

	public void append( Object value )
	{
		this.values.add( value );
	}

	public List<Object> getValues()
	{
		return this.values;
	}

	public TupleValue unwrap()
	{
		TupleValue result = new TupleValue();
		for( Object object : this.values )
			if( object instanceof Value )
				result.append( ( (Value)object ).get() );
			else
				result.append( object );
		return result;
	}

	public int size()
	{
		return this.values.size();
	}

	public Object get( int index )
	{
		return this.values.get( index );
	}

	public Object getLast()
	{
		Object result = this.values.get( this.values.size() - 1 );
		if( result instanceof TupleValue )
			return ( (TupleValue)result ).getLast();
		return result;
	}
}
