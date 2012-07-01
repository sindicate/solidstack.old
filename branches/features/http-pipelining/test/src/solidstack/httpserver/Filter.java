package solidstack.httpserver;

public interface Filter
{
	Response call( RequestContext context, FilterChain chain );
}
