package solidstack.httpserver;


public interface Servlet
{
	void call( RequestContext context );
}
