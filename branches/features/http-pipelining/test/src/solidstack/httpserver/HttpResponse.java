package solidstack.httpserver;




abstract public class HttpResponse
{
	abstract public void write( ResponseOutputStream out );

//	static protected int count = 1;

//	protected Request request;
//	protected ResponseOutputStream out;
//	protected ResponseWriter writer;
//	protected Map< String, List< String > > headers = new HashMap< String, List<String> >();
//
//	protected Response()
//	{
//	}
//
//	public Response( Request request, OutputStream out )
//	{
//		this.request = request;
//		this.out = new ResponseOutputStream( this, out );
//	}
//
//	public ResponseOutputStream getOutputStream()
//	{
//		return this.out;
//	}
//
//	public ResponseWriter getWriter()
//	{
//		if( this.writer != null )
//			return this.writer;
//		if( this.charSet != null )
//			return getWriter( this.charSet );
//		return getWriter( "ISO-8859-1" );
//	}
//
//	public ResponseWriter getWriter( String encoding )
//	{
//		if( this.writer != null )
//		{
//			if( this.writer.getEncoding().equals( encoding ) )
//				return this.writer;
//			this.writer.flush();
//		}
//		return this.writer = new ResponseWriter( this.out, encoding );
//	}
//
//	public PrintWriter getPrintWriter( String encoding )
//	{
//		return new PrintWriter( getWriter( encoding ) );
//	}
//
//	static public final byte[] HTTP = "HTTP/1.1 ".getBytes();
//	static public final byte[] NEWLINE = new byte[] { '\r', '\n' };
//	static public final byte[] COLON = new byte[] { ':', ' ' };
//
//	public void reset()
//	{
//		if( this.out.committed )
//			throw new IllegalStateException( "Response is already committed" );
//		getOutputStream().clear();
//		this.writer = null;
//		this.statusCode = 200;
//		this.statusMessage = "OK";
//		this.headers.clear();
//	}
//
//	public void flush()
//	{
//		if( this.writer != null )
//			this.writer.flush();
//		getOutputStream().flush();
//	}
//
//	public void finish()
//	{
//		flush();
//		getOutputStream().close();
//	}
//
//	public String getHeader( String name )
//	{
//		List< String > values = this.headers.get( name );
//		if( values == null )
//			return null;
//		Assert.isTrue( !values.isEmpty() );
//		if( values.size() > 1 )
//			throw new IllegalStateException( "Found more than 1 value for the header " + name );
//		return values.get( 0 );
//	}
}
