package solidstack.httpserver;

import java.util.Map;

public class RequestContext
{
	protected Request request;
	protected Session session;
	protected ApplicationContext applicationContext;
	protected Map< String, Object > args;

	// TODO Parameter order
	public RequestContext( Request request, ApplicationContext applicationContext )
	{
		this.request = request;
		this.applicationContext = applicationContext;
	}

	// TODO Parameter order
	public RequestContext( RequestContext parent, String path, Map< String, Object > args )
	{
		this.request = new Request();
		this.request.setUrl( path );
		this.request.parameters = parent.getRequest().getParameters();

		this.session = parent.getSession();
		this.applicationContext = parent.getApplication();
		this.args = args;
	}

	public Request getRequest()
	{
		return this.request;
	}

//	public void callJsp( String jsp )
//	{
//		this.applicationContext.callJsp( jsp, this );
//	}

	public ApplicationContext getApplication()
	{
		return this.applicationContext;
	}

	public Object getArgs()
	{
		return this.args;
	}

	public Response include( String path, Map< String, Object > args )
	{
		RequestContext context = new RequestContext( this, path, args );
		return getApplication().dispatchInternal( context );
	}

	public Response include( String path )
	{
		return include( path, null );
	}

	public void setSession( Session session )
	{
		this.session = session;
	}

	public Session getSession()
	{
		return this.session;
	}
}
