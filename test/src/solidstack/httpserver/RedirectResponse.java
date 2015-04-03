package solidstack.httpserver;


public class RedirectResponse extends HttpResponse
{
	private String location;

	public RedirectResponse( String location )
	{
		this.location = location;
	}

	@Override
	public void write( ResponseOutputStream out )
	{
//		response.reset(); Do not reset, we need the Set-Cookies
		out.setStatusCode( 303, "Redirect" );
		out.setHeader( "Location", this.location );
	}
}
