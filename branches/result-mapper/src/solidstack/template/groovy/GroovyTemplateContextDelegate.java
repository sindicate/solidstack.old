package solidstack.template.groovy;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;

import org.codehaus.groovy.runtime.InvokerHelper;

import solidstack.template.TemplateContext;

public class GroovyTemplateContextDelegate implements GroovyObject
{
	private TemplateContext context;

	public GroovyTemplateContextDelegate( TemplateContext context )
	{
		this.context = context;
	}

	public Object getProperty( String property )
	{
		return InvokerHelper.getProperty( this.context.getParameters(), property );
	}

	public Object invokeMethod( String name, Object args )
	{
		try
		{
			return InvokerHelper.invokeMethod( this.context.getParameters(), name, args );
		}
		catch( MissingMethodException e )
		{
			return InvokerHelper.invokeMethod( this.context, name, args );
		}
	}

	public void setProperty( String property, Object newValue )
	{
		InvokerHelper.setProperty( this.context, property, newValue );
	}

	public MetaClass getMetaClass()
	{
		return InvokerHelper.getMetaClass( getClass() );
	}

	public void setMetaClass( MetaClass metaClass )
	{
		throw new UnsupportedOperationException();
	}
}
