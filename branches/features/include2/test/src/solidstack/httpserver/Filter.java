package solidstack.httpserver;

public interface Filter
{
	void call( RequestContext request, FilterChain chain );
}
