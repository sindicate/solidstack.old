package solidstack.httpserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Request
{
	protected String url;
	protected String query;
	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
	protected Map< String, Object > parameters = new HashMap< String, Object >();
	protected String fragment;

	public void setUrl( String url )
	{
		this.url = url;
	}

	public String getUrl()
	{
		return this.url;
	}

	public void setQuery( String query )
	{
		this.query = query;
	}

	public void addParameter( String name, String value )
	{
		Object elem = this.parameters.get( name );
		if( elem == null )
			this.parameters.put( name, value );
		else
		{
			List< String > values = new ArrayList<String>();
			values.add( (String)elem );
			values.add( value );
			this.parameters.put( name, values );
		}
	}

	public void addHeader( String name, String value )
	{
		List<String> values = this.headers.get( name );
		if( values == null )
			this.headers.put( name, values = new ArrayList< String >() );
		values.add( value );
	}

	@SuppressWarnings( "unchecked" )
	public String getParameter( String name )
	{
		Object elem = this.parameters.get( name );
		if( elem instanceof List )
			return ( (List< String >)elem ).get( 0 );
		return (String)elem;
	}

	public Map< String, Object > getParameters()
	{
		return this.parameters;
	}

	public String getHeader( String name )
	{
		List< String > values = this.headers.get( name );
		if( values == null )
			return null;
		Assert.isTrue( !values.isEmpty() );
		if( values.size() > 1 )
			throw new IllegalStateException( "Found more than 1 value for the header " + name );
		return values.get( 0 );
	}

	public boolean isConnectionClose()
	{
		return "close".equals( getHeader( "Connection" ) );
	}
}
