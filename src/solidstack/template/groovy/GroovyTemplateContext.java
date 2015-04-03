package solidstack.template.groovy;

import java.io.IOException;
import java.util.Map;

import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateContext;


public class GroovyTemplateContext extends TemplateContext
{
	public GroovyTemplateContext( Template template, EncodingWriter writer, Map<String, Object> args )
	{
		super( template, writer, args );
	}

	public GroovyTemplateContext( Template template, TemplateContext parent, Map<String, Object> args )
	{
		super( template, parent, args );
	}

	public void include( Map<String, Object> args, String path ) throws IOException // TODO Remove throw or not?
	{
		Template template = getTemplate().getManager().getTemplate( path );
		template.apply( this, args );
	}
}
