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

package solidstack.template;

import groovy.lang.Closure;
import groovy.lang.GString;

import java.io.IOException;

import org.codehaus.groovy.runtime.InvokerHelper;


/**
 * A ConvertingWriter that converts Groovy specific data types to Java data types.
 * 
 * @author René de Bloois
 */
public class GroovyConvertingWriter implements ConvertingWriter
{
	/**
	 * The EncodingWriter to write to.
	 */
	protected EncodingWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param writer The EncodingWriter to write to.
	 */
	public GroovyConvertingWriter( EncodingWriter writer )
	{
		this.writer = writer;
	}

	public void write( Object o ) throws IOException
	{
		if( o == null )
			this.writer.write( null );
		else if( o instanceof String )
			this.writer.write( (String)o );
		else if( o instanceof GString )
		{
			GString gString = (GString)o;
			String[] strings = gString.getStrings();
			Object[] values = gString.getValues();
			if( !( strings.length == values.length + 1 ) )
				throw new IllegalStateException();

			for( int i = 0; i < values.length; i++ )
			{
				this.writer.write( strings[ i ] );
				writeEncoded( values[ i ] );
			}
			this.writer.write( strings[ values.length ] );
		}
		else if( o instanceof Closure )
		{
			Closure c = (Closure)o;
			int pars = c.getMaximumNumberOfParameters();
			if( pars > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			write( c.call() ); // May be recursive
		}
		else
			this.writer.write( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		if( this.writer.supportsValues() )
		{
			if( o instanceof GString )
				this.writer.writeValue( o.toString() );
			else if( o instanceof Closure )
			{
				Closure c = (Closure)o;
				int pars = c.getMaximumNumberOfParameters();
				if( pars > 0 )
					throw new TemplateException( "Closures with parameters are not supported in expressions." );
				this.writer.writeValue( c.call() );
			}
			else
				this.writer.writeValue( o );
		}
		else
		{
			if( o == null )
				this.writer.writeEncoded( null );
			else if( o instanceof String )
				this.writer.writeEncoded( (String)o );
			else if( o instanceof GString )
				this.writer.writeEncoded( o.toString() );
			else if( o instanceof Closure )
			{
				Closure c = (Closure)o;
				int pars = c.getMaximumNumberOfParameters();
				if( pars > 0 )
					throw new TemplateException( "Closures with parameters are not supported in expressions." );
				writeEncoded( c.call() ); // May be recursive
			}
			else
				this.writer.writeEncoded( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
		}
	}
}
