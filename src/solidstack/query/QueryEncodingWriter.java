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

package solidstack.query;

import groovy.lang.Closure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

import solidstack.template.EncodingWriter;
import solidstack.template.TemplateException;

/**
 * An encoding writer. Adds a {@link #writeEncoded(String)} method. This implementation does not encode.
 * 
 * @author René M. de Bloois
 *
 */
// Can't implement Writer. DefaultGroovyMethods.write(Writer self, Writable writable) will be called when value is null, which results in NPE.
public class QueryEncodingWriter implements EncodingWriter
{
	private List< Object > values = new ArrayList< Object >();
	private BitSet isValue = new BitSet();

	public void write( String s ) throws IOException
	{
		if( s != null )
			this.values.add( s );
	}

	public void write( Object o ) throws IOException
	{
		if( o != null )
			this.values.add( InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}

	public void write( Closure c ) throws IOException
	{
		if( c != null )
		{
			int pars = c.getMaximumNumberOfParameters();
			if( pars > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			Object result = c.call();
			if( result != null )
				this.values.add( InvokerHelper.invokeMethod( result, "asType", String.class ) );
		}
	}

	public void writeEncoded( String s ) throws IOException
	{
		if( s == null )
			return;
		this.isValue.set( this.values.size() );
		write( s );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		if( o != null )
			writeEncoded( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}

	public void writeEncoded( Closure c ) throws IOException
	{
		if( c != null )
		{
			int pars = c.getMaximumNumberOfParameters();
			if( pars > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			Object result = c.call();
			if( result != null )
				writeEncoded( result );
		}
	}

	public List< Object > getValues()
	{
		return this.values;
	}

	public BitSet getIsValue()
	{
		return this.isValue;
	}
}
