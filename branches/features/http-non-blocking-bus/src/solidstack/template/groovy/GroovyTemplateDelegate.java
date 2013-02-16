package solidstack.template.groovy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import solidstack.io.FatalURISyntaxException;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.util.Pars;

public class GroovyTemplateDelegate implements Map< String, Object >
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

	public void include( Map< String, Object > args )
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

		Pars pars = new Pars( this.parameters ).set( "args", args );
		template.apply( pars, this.writer );
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsValue( Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object get( Object key )
	{
		return this.parameters.get( key );
	}

	public Object put( String key, Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object remove( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public void putAll( Map<? extends String, ? extends Object> m )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set<String> keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection<Object> values()
	{
		throw new UnsupportedOperationException();
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet()
	{
		throw new UnsupportedOperationException();
	}
}