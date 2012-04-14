package solidstack.httpserver;

import java.util.Map;

public class RequestContext
{
	protected Request request;
	protected Response reponse;
	protected Session session;
	protected ApplicationContext applicationContext;
	protected Map< String, Object > args;

	// TODO Parameter order
	public RequestContext( Request request, Response response, ApplicationContext applicationContext )
	{
		this.request = request;
		this.reponse = response;
		this.applicationContext = applicationContext;
	}

	// TODO Parameter order
	public RequestContext( RequestContext parent, String path, Map< String, Object > args )
	{
		this.request = new Request();
		this.request.setUrl( path );
		this.request.parameters = parent.getRequest().getParameters();

		this.session = parent.getSession();
		this.reponse = parent.getResponse();
		this.applicationContext = parent.getApplication();
		this.args = args;
	}

	public Request getRequest()
	{
		return this.request;
	}

	public Response getResponse()
	{
		return this.reponse;
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

	public void include( String path, Map< String, Object > args )
	{
		RequestContext context = new RequestContext( this, path, args );
		getApplication().dispatchInternal( context );
	}

	public void include( String path )
	{
		include( path, null );
	}

	public void setSession( Session session )
	{
		this.session = session;
	}

	public Session getSession()
	{
		return this.session;
	}

	public void redirect( String path )
	{
		Response response = getResponse();
//		response.reset(); Do not reset, we need the Set-Cookies
		response.setStatusCode( 303, "Redirect" );
		response.setHeader( "Location", path );
	}
}
