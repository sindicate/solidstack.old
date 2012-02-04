package solidstack.template;

import groovy.lang.Closure;
import groovy.lang.GString;

import java.io.IOException;

import org.codehaus.groovy.runtime.InvokerHelper;

public class GroovyConvertingWriter implements ConvertingWriter
{
	protected EncodingWriter writer;

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
		else if( o instanceof GString && this.writer.supportsValues() )
		{
			GString gString = (GString)o;
			String[] strings = gString.getStrings();
			Object[] values = gString.getValues();
			if( !( strings.length == values.length + 1 ) )
				throw new IllegalStateException();

			for( int i = 0; i < values.length; i++ )
			{
				this.writer.write( strings[ i ] );
				this.writer.writeValue( values[ i ] );
			}
			write( strings[ values.length ] );
		}
		else if( o instanceof Closure )
		{
			Closure c = (Closure)o;
			int pars = c.getMaximumNumberOfParameters();
			if( pars > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			write( c.call() );
		}
		else
			this.writer.write( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}

	public void writeEncoded( Object o ) throws IOException
	{
		if( this.writer.supportsValues() )
			if( o instanceof GString )
				this.writer.writeValue( o.toString() );
			else if( o instanceof Closure )
			{
				Closure c = (Closure)o;
				int pars = c.getMaximumNumberOfParameters();
				if( pars > 0 )
					throw new TemplateException( "Closures with parameters are not supported in expressions." );
				writeEncoded( c.call() );
			}
			else
				this.writer.writeValue( o );
		else if( o == null )
			this.writer.writeEncoded( null );
		else if( o instanceof String )
			this.writer.writeEncoded( (String)o );
		else if( o instanceof Closure )
		{
			Closure c = (Closure)o;
			int pars = c.getMaximumNumberOfParameters();
			if( pars > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			writeEncoded( c.call() );
		}
		else
			this.writer.writeEncoded( (String)InvokerHelper.invokeMethod( o, "asType", String.class ) );
	}
}
