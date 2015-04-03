package solidstack.template.groovy;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.codehaus.groovy.runtime.InvokerHelper;

import solidstack.template.EncodingWriter;
import solidstack.template.TemplateContext;

public class GroovyTemplateDelegate extends TemplateContext implements GroovyObject
{
	public GroovyTemplateDelegate( GroovyTemplate template, Object parameters, EncodingWriter writer )
	{
		super( template, parameters, writer );
	}

	public Object getProperty( String property )
	{
		return InvokerHelper.getProperty( this.parameters, property );
	}

	public Object invokeMethod( String name, Object args )
	{
		return InvokerHelper.invokeMethod( this.parameters, name, args );
	}

	public void setProperty( String property, Object newValue )
	{
		InvokerHelper.setProperty( this.parameters, property, newValue );
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
