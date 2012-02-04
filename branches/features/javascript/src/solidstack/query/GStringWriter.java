/*--
 * Copyright 2006 René M. de Bloois
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import solidstack.template.EncodingWriter;


/**
 * A writer that accepts Groovy's {@link GString} and keeps the values of the GStrings separate from the string segments.
 * 
 * @author René M. de Bloois
 */
public class GStringWriter implements EncodingWriter
{
	private List< Object > values = new ArrayList< Object >();
	private BitSet isValue = new BitSet();

	//@Override
	public void write( String s )
	{
		if( s != null && s.length() > 0 )
			this.values.add( s );
	}

	//@Override
	public void writeEncoded( String s ) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	//@Override
	public void writeValue( Object o )
	{
		this.isValue.set( this.values.size() );
		this.values.add( o );
	}

	public boolean supportsValues()
	{
		return true;
	}

	/**
	 * Returns the string segments and the values.
	 * 
	 * @return An array of string segments (String) and the values (unknown Object).
	 */
	public List< Object > getValues()
	{
		return this.values;
	}

	/**
	 * Returns a bitset that indicates which indexes in the {@link #getValues()} list is a value.
	 * 
	 * @return A bitset that indicates which indexes in the {@link #getValues()} list is a value.
	 */
	public BitSet getIsValue()
	{
		return this.isValue;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		int len = this.values.size();
		for( int i = 0; i < len; i++ )
		{
			if( this.isValue.get( i ) )
				result.append( this.values.get( i ).toString() );
			else
				result.append( (String)this.values.get( i ) );
		}
		return result.toString();
	}
}
