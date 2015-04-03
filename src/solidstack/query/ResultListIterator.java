/*--
 * Copyright 2010 Ren� M. de Bloois
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

package solidstack.query;

import java.util.ListIterator;
import java.util.Map;


/**
 * Wraps a iterator from the list of arrays and gives Map instances instead of arrays.
 * 
 * @author Ren� M. de Bloois
 */
public class ResultListIterator implements ListIterator< Map< String, Object >>
{
	private ListIterator<Object[]> iterator;
	private Map< String, Integer > names;

	/**
	 * Constructor.
	 * 
	 * @param iterator Iterator over the list of arrays.
	 * @param names Maps of names to indexes.
	 */
	public ResultListIterator( ListIterator<Object[]> iterator, Map< String, Integer > names )
	{
		this.iterator = iterator;
		this.names = names;
	}

	public boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	public Map< String, Object > next()
	{
		return new ValuesMap( this.names, this.iterator.next() );
	}

	public boolean hasPrevious()
	{
		return this.iterator.hasPrevious();
	}

	public int nextIndex()
	{
		return this.iterator.nextIndex();
	}

	public Map< String, Object > previous()
	{
		return new ValuesMap( this.names, this.iterator.previous() );
	}

	public int previousIndex()
	{
		return this.iterator.previousIndex();
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public void add( Map< String, Object > e )
	{
		throw new UnsupportedOperationException();
	}

	public void set( Map< String, Object > e )
	{
		throw new UnsupportedOperationException();
	}
}
