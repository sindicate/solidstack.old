package solidstack.httpserver;


public interface Servlet
{
	Response call( RequestContext context );
}
