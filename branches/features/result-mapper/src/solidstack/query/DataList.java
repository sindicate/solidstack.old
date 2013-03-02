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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Represents a list of rows. The rows have a type described by {@link DataObjectType}. The rows behave like a map, but with case
 * insensitive keys.
 *
 * @author René M. de Bloois
 */
// TODO Rename RowList -> Table?
// TODO Add Index capability?
public class DataList implements List<DataObject>, Serializable
{
	private static final long serialVersionUID = 1L;

	private DataObjectType type; // When all objects have the same type
	private List<DataObject> list; // TODO Use array of Object[]

	/**
	 * Constructor.
	 */
	public DataList()
	{
		this.list = new ArrayList<DataObject>();
	}

	/**
	 * @param list The list of arrays.
	 */
	public DataList( List<DataObject> list )
	{
		this.list = list;
	}

	/**
	 * @param type The row type.
	 * @param tuples The list of arrays.
	 */
	public DataList( DataObjectType type, List<Object[]> tuples )
	{
		this.type = type;
		this.list = new ArrayList<DataObject>();
		for( Object[] tuple : tuples )
			this.list.add( new DataObject( type, tuple ) );
	}

	/**
	 * @return The tuples.
	 */
	public List<DataObject> getObjects()
	{
		return this.list;
	}

	/**
	 * @return The row type.
	 */
	public DataObjectType getType()
	{
		return this.type;
	}

	/**
	 * Adds a tuple.
	 *
	 * @param object The data object to add.
	 */
	public boolean add( DataObject object )
	{
		// TODO Check that the type equals the this.type
		this.list.add( object );
		return true;
	}

	public DataObject get( int index )
	{
		return this.list.get( index );
	}

	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	public Iterator<DataObject> iterator()
	{
		return this.list.iterator();
	}

	public ListIterator<DataObject> listIterator()
	{
		return this.list.listIterator();
	}

	public ListIterator<DataObject> listIterator( int index )
	{
		return this.list.listIterator( index );
	}

	public int size()
	{
		return this.list.size();
	}

	// TODO We now can implement more of these below

	public void add( int arg0, DataObject arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( Collection<? extends DataObject> arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll( int arg0, Collection<? extends DataObject> arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean contains( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsAll( Collection<?> arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public int indexOf( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public int lastIndexOf( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean remove( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public DataObject remove( int arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll( Collection<?> arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll( Collection<?> arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public DataObject set( int arg0, DataObject arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public List<DataObject> subList( int arg0, int arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public DataObject[] toArray()
	{
		throw new UnsupportedOperationException();
//		@SuppressWarnings( "unchecked" )
//		Map< String, Object>[] result = new Map[ this.list.size() ];
//		Iterator< Map< String, Object > > i = iterator();
//		int j = 0;
//		while( i.hasNext() )
//			result[ j++ ] = i.next();
//		return result;
	}

	public <T> T[] toArray( T[] arg0 )
	{
		throw new UnsupportedOperationException();
	}

//	private void writeAsHTML( EncodingWriter w ) throws IOException
//	{
//		w.write( "<table>" );
//		w.write( "<tr>" );
//		for( String name : this.type.getAttributeIndex().keySet() )
//		{
//			w.write( "<th>" );
//			w.writeEncoded( name );
//			w.write( "</th>" );
//		}
//		w.write( "</tr>" );
//		for( Object[] tuple : this.list )
//		{
//			w.write( "<tr>" );
//			for( Object value : tuple )
//				if( value == null )
//					w.write( "<td class=\"null\" />" );
//				else
//				{
//					w.write( "<td>" );
//					if( value instanceof DataList )
//						( (DataList)value ).writeAsHTML( w );
//					else if( value instanceof DataObject )
//						w.write( "(row)" );
//					else
//						w.writeEncoded( value.toString() );
//					w.write( "</td>" );
//				}
//			w.write( "</tr>" );
//		}
//		w.write( "</table>" );
//	}
//
//	public void writeAsHTML( Writer out ) throws IOException
//	{
//		EncodingWriter w = new XMLEncodingWriter( out );
//		w.write( "<html>" );
//		w.write( "<head>" );
//		w.write( "<style type=\"text/css\">" );
//		w.write( "td { border: 1px solid black; vertical-align: top; font: small sans-serif; } th { border: 1px solid black; vertical-align: top; font: small sans-serif; background-color: #EEE; } table { border: 1px solid black; border-collapse: collapse; }" );
//		w.write( "</style>" );
//		w.write( "</head>" );
//		w.write( "<body>" );
//		writeAsHTML( w );
//		w.write( "</body>" );
//		w.write( "</html>" );
//	}
}
