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

	public void call( RequestContext request )
	{
		Assert.notNull( this.servlet );
		if( this.filters.isEmpty() )
			this.servlet.call( request, null );
		else
		{
			Filter filter = this.filters.remove( 0 );
			filter.call( request, this );
		}
	}
}
