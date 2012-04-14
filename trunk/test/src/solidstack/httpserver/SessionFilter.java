package solidstack.httpserver;

import java.util.HashMap;
import java.util.Map;


public class SessionFilter implements Filter
{
	private int sessionid;
	private Map< String, Session > sessions = new HashMap<String, Session>();

	public void call( RequestContext context, FilterChain chain )
	{
		// TODO Synchronization
		String sessionId = context.getRequest().getCookie( "SESSIONID" );
		Session session = null;
		if( sessionId != null )
			session = this.sessions.get( sessionId );
		if( session != null )
			context.setSession( session );
		else
		{
			session = new Session();
			context.setSession( session );
			sessionId = Integer.toString( ++this.sessionid );
			this.sessions.put( sessionId, session );
			context.getResponse().setCookie( "SESSIONID", sessionId );
		}
		chain.call( context );
	}
}
