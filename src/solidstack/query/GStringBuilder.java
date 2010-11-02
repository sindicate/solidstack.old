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

import groovy.lang.GString;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.runtime.GStringImpl;

/**
 * 
 * @author René M. de Bloois
 */
public class GStringBuilder
{
	protected List< String > strings = new ArrayList();
	protected List< Object > values = new ArrayList();

	public GStringBuilder append( GString s )
	{
		if( !( this.strings.size() == 0 || this.strings.size() == this.values.size() + 1 ) )
			throw new IllegalStateException();

		String[] strings = s.getStrings();
		Object[] values = s.getValues();
		if( !( strings.length == values.length + 1 ) )
			throw new IllegalStateException();

		append( strings[ 0 ] );

		for( int i = 0; i < values.length; i++ )
			this.values.add( values[ i ] );
		for( int i = 1; i < strings.length; i++ )
			this.strings.add( strings[ i ] );

		if( !( this.strings.size() == this.values.size() + 1 ) )
			throw new IllegalStateException();
		return this;
	}

	public GStringBuilder append( String s )
	{
		if( !( this.strings.size() == 0 || this.strings.size() == this.values.size() + 1 ) )
			throw new IllegalStateException();

		int last = this.strings.size() - 1;
		if( last >= 0 )
			this.strings.set( last, this.strings.get( last ) + s );
		else
			this.strings.add( s );

		if( !( this.strings.size() == this.values.size() + 1 ) )
			throw new IllegalStateException();
		return this;
	}

	public GStringBuilder append( Object o )
	{
		return append( o.toString() );
	}

	public GString toGString()
	{
		int size = this.values.size();
		if( !( this.strings.size() == size + 1 ) )
			throw new IllegalStateException();
		return new GStringImpl( this.values.toArray( new Object[ size ] ), this.strings.toArray( new String[ size + 1 ] ) );
	}
}
