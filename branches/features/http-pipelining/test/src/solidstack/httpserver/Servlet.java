package solidstack.httpserver;


public interface Servlet
{
	HttpResponse call( RequestContext context );
}
