package solidstack.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SessionFilter implements Filter
{
	private int sessionid;
	private Map< String, Session > sessions = new HashMap<String, Session>();

	public Response call( RequestContext context, FilterChain chain )
	{
		// TODO Synchronization
		String sessionId = context.getRequest().getCookie( "SESSIONID" );
		Loggers.httpServer.debug( "session: {}", sessionId );

		Session session = null;
		if( sessionId != null )
			session = this.sessions.get( sessionId );

		if( session != null )
		{
			context.setSession( session );
			return chain.call( context );
		}

		session = new Session();
		context.setSession( session );
		final String newSessionId = Integer.toString( ++this.sessionid );
		this.sessions.put( newSessionId, session );
		Loggers.httpServer.debug( "setCookie: session: {}", newSessionId );

		final Response response = chain.call( context );
		return new Response()
		{
			@Override
			public void write( ResponseOutputStream out ) throws IOException
			{
				out.setCookie( "SESSIONID", newSessionId );
				response.write( out );
			}
		};
	}
}
