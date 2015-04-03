package solidstack.httpserver;

import java.util.HashMap;
import java.util.Map;


public class SessionFilter implements Filter
{
	private volatile int sessionid;
	private Map< String, Session > sessions = new HashMap<String, Session>();

	public HttpResponse call( RequestContext context, FilterChain chain )
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

		final HttpResponse response = chain.call( context );
		return new HttpResponse()
		{
			@Override
			public void write( ResponseOutputStream out )
			{
				Loggers.httpServer.debug( "setCookie: session: {}", newSessionId );
				out.setCookie( "SESSIONID", newSessionId );
				response.write( out );
			}
		};
	}
}
