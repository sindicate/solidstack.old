package solidstack.template.groovy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import solidstack.io.FatalURISyntaxException;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateLoader;

public class GroovyTemplateDelegate
{
	private GroovyTemplate template;
	private Map< String, Object > parameters;
	private EncodingWriter writer;

	public GroovyTemplateDelegate( GroovyTemplate template, Map< String, Object > parameters, EncodingWriter writer )
	{
		this.template = template;
		this.parameters = parameters;
		this.writer = writer;
	}

	public void include( Map args )
	{
		String path = (String)args.get( "template" );
		if( !path.startsWith( "/" ) )
		{
			// TODO Make util
			try
			{
				URI uri = new URI( this.template.getPath() );
				uri = uri.resolve( path );
				path = uri.getPath();
			}
			catch( URISyntaxException e )
			{
				throw new FatalURISyntaxException( e );
			}
		}
		TemplateLoader loader = this.template.getLoader();
		Template template = loader.getTemplate( path );
		template.apply( args, this.writer );
	}
}
