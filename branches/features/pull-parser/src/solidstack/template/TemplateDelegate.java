package solidstack.template;

import groovy.lang.GroovyObjectSupport;

import java.util.Map;

// TODO This class is not used at the moment.
public class TemplateDelegate extends GroovyObjectSupport
{
	protected Map< String, ? > params;

	public TemplateDelegate( Map< String, ? > params )
	{
		this.params = params;
	}

	@Override
	public Object getProperty( String name )
	{
		return this.params.get( name );
	}

	public String escape( String text )
	{
		return text;
	}
}
