package solidstack.httpserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Request
{
	protected String method;
	protected String url;
	protected String query;
	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
	protected Map< String, String > cookies = new HashMap< String, String >();
	protected Map< String, Object > parameters = new HashMap< String, Object >();
	protected String fragment;

	public void setMethod( String method )
	{
		this.method = method;
	}

	public String getMethod()
	{
		return this.method;
	}

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

	@SuppressWarnings( "unchecked" )
	public void addParameter( String name, String value )
	{
		Object elem = this.parameters.get( name );
		if( elem == null )
			this.parameters.put( name, value );
		else if( elem instanceof List )
			( (List<String>)elem ).add( value );
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

	public void addCookie( String name, String value )
	{
		this.cookies.put( name, value );
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

	public String getCookie( String name )
	{
		return this.cookies.get( name );
	}

	public boolean isConnectionClose()
	{
		return "close".equals( getHeader( "Connection" ) );
	}
}
