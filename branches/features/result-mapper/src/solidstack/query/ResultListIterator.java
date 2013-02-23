/*--
 * Copyright 2010 René M. de Bloois
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
 * Wraps a iterator from the list of arrays and returns Map instances instead of arrays.
 *
 * @author René M. de Bloois
 */
public class ResultListIterator implements ListIterator< Map< String, Object >>
{
	private RowType type;
	private ListIterator<Object[]> iterator;

	/**
	 * Constructor.
	 *
	 * @param type The type of the rows.
	 * @param iterator Iterator over the list of arrays.
	 */
	public ResultListIterator( RowType type, ListIterator<Object[]> iterator )
	{
		this.type = type;
		this.iterator = iterator;
	}

	public boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	public Map< String, Object > next()
	{
		return new Row( this.type, this.iterator.next() );
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
		return new Row( this.type, this.iterator.previous() );
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
