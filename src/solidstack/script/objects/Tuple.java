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

package solidstack.script.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Tuple
{
	static public Tuple apply( Object... values )
	{
		return new Tuple( values );
	}

	private List<Object> values = new ArrayList<Object>(); // TODO Should be array

	public Tuple()
	{
	}

	public Tuple( Object... values )
	{
		Collections.addAll( this.values, values );
	}

	public Tuple( List<Object> values )
	{
		this.values = values;
	}

	public Tuple append( Object value )
	{
		this.values.add( value );
		return this;
	}

	public List<Object> list()
	{
		return this.values;
	}

	public int size()
	{
		return this.values.size();
	}

	public Object get( int index )
	{
		return this.values.get( index );
	}

	// TODO update()?
	public Object apply( int index )
	{
		return get( index );
	}

	public Object getLast()
	{
		Object result = this.values.get( this.values.size() - 1 );
		if( result instanceof Tuple )
			return ( (Tuple)result ).getLast();
		return result;
	}
}
