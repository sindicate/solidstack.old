package solidstack.httpserver;

import java.util.Map;

public class RequestContext
{
	protected Request request;
	protected Response reponse;
	protected ApplicationContext applicationContext;
	protected Map< String, Object > args;

	public RequestContext( Request request, Response response, ApplicationContext applicationContext )
	{
		this.request = request;
		this.reponse = response;
		this.applicationContext = applicationContext;
	}

	public RequestContext( RequestContext parent, Map< String, Object > args )
	{
		this.request = parent.getRequest();
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
}
