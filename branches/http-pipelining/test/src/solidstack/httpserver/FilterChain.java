package solidstack.httpserver;

import java.util.ArrayList;
import java.util.List;

import solidstack.lang.Assert;


public class FilterChain
{
	protected List< Filter > filters = new ArrayList< Filter >();
	protected Servlet servlet;

	public void add( Filter filter )
	{
		this.filters.add( filter );
	}

	public void set( Servlet servlet )
	{
		this.servlet = servlet;
	}

	public HttpResponse call( RequestContext context )
	{
		Assert.notNull( this.servlet );
		if( this.filters.isEmpty() )
			return this.servlet.call( context );
		Filter filter = this.filters.remove( 0 );
		return filter.call( context, this );
	}
}
