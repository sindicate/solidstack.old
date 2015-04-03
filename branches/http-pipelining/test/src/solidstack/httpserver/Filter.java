package solidstack.httpserver;

public interface Filter
{
	HttpResponse call( RequestContext context, FilterChain chain );
}
