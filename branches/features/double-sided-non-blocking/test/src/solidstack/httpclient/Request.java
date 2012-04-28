package solidstack.httpclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Request
{
//	static protected int count = 1;

	// TODO GET or POST
	private String path;
	private Map< String, List< String > > headers = new HashMap< String, List<String> >();

	public Request( String path )
	{
		setPath( path );
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setHeader( String name, String value )
	{
		List< String > values = new ArrayList< String >();
		values.add( value );
		getHeaders().put( name, values );
	}

	public String getHeader( String name )
	{
		List< String > values = getHeaders().get( name );
		if( values == null )
			return null;
		Assert.isTrue( !values.isEmpty() );
		if( values.size() > 1 )
			throw new IllegalStateException( "Found more than 1 value for the header " + name );
		return values.get( 0 );
	}

	public Map< String, List< String > > getHeaders()
	{
		return this.headers;
	}
}
