package solidstack.httpserver;

import java.io.IOException;

public class RedirectResponse extends Response
{
	private String location;

	public RedirectResponse( String location )
	{
		this.location = location;
	}

	@Override
	public void write( ResponseOutputStream out ) throws IOException
	{
//		response.reset(); Do not reset, we need the Set-Cookies
		out.setStatusCode( 303, "Redirect" );
		out.setHeader( "Location", this.location );
	}
}
