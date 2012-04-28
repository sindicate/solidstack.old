package solidstack.httpserver;

public interface Filter
{
	void call( RequestContext context, FilterChain chain );
}
