package solidstack.httpserver;

import solidstack.util.Pars;

public interface Servlet
{
	void call( RequestContext request, Pars params );
}
