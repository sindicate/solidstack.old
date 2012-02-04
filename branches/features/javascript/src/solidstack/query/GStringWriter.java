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

import groovy.lang.Closure;
import groovy.lang.GString;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

import solidstack.template.EncodingWriter;
import solidstack.template.TemplateException;

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
	public void write( Object o )
	{
		if( o != null )
			if( o instanceof String )
				writeString( (String)o );
			else if( o instanceof GString )
				writeGString( (GString)o );
			else if( o instanceof Closure )
			{
				Closure c = (Closure)o;
				int pars = c.getMaximumNumberOfParameters();
				if( pars > 0 )
					throw new TemplateException( "Closures with parameters are not supported in expressions." );
				write( c.call() );
			}
			else
				writeString( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}

	//@Override
	public void writeEncoded( Object o )
	{
		this.isValue.set( this.values.size() );
		this.values.add( o );
	}

	protected void writeString( String string )
	{
		if( string != null && string.length() > 0 )
			this.values.add( string );
	}

	/**
	 * Append a {@link GString}. The values of the GString are kept separate from the string segments.
	 * 
	 * @param gString The {@link GString} to append.
	 */
	protected void writeGString( GString gString )
	{
		String[] strings = gString.getStrings();
		Object[] values = gString.getValues();
		if( !( strings.length == values.length + 1 ) )
			throw new IllegalStateException();

		for( int i = 0; i < values.length; i++ )
		{
			writeString( strings[ i ] );
			writeEncoded( values[ i ] );
		}
		writeString( strings[ values.length ] );
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
				result.append( InvokerHelper.invokeMethod( this.values.get( i ), "asType", String.class ) );
			else
				result.append( (String)this.values.get( i ) );
		}
		return result.toString();
	}
}
