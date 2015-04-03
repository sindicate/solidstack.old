package solidstack.template;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import solidstack.io.FatalURISyntaxException;
import solidstack.util.Pars;

public class TemplateContext
{
	private Template template;
	protected Object parameters;
	private EncodingWriter writer;

	public TemplateContext( Template template, Object parameters, EncodingWriter writer )
	{
		this.template = template;
		this.parameters = parameters;
		this.writer = writer;
	}

	public EncodingWriter getWriter()
	{
		return this.writer;
	}

	public Object getParameters()
	{
		return this.parameters;
	}

	public void include( Map< String, Object > args )
	{
		String path = (String)args.get( "template" );
		if( !path.startsWith( "/" ) )
		{
			// TODO Use resources
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

		Pars pars = new Pars( this.parameters ).set( "args", args ); // FIXME This is not gonna work, this.parameters is an object
		template.apply( pars, this.writer );
	}
}
