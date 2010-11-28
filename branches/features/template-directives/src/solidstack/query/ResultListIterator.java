package solidstack.query;

import java.util.ListIterator;
import java.util.Map;


/**
 * Wraps a iterator from the list of arrays and gives Map instances instead of arrays.
 * 
 * @author René M. de Bloois
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
