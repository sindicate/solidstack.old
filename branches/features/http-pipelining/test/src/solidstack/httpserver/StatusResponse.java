package solidstack.httpserver;

public class StatusResponse extends Response
{
	private int statusCode = 200;
	private String statusMessage = "OK";

	public StatusResponse( int code, String message )
	{
		this.statusCode = code;
		this.statusMessage = message;
	}

	@Override
	public void write( ResponseOutputStream out )
	{
		out.setStatusCode( this.statusCode, this.statusMessage );
	}
}
