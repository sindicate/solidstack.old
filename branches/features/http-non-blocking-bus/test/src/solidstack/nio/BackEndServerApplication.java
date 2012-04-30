package solidstack.nio;

import solidstack.httpserver.ApplicationContext;

public class BackEndServerApplication extends ApplicationContext
{
	public BackEndServerApplication()
	{
		registerServlet( "", new BackEndRootServlet() );
	}
}
