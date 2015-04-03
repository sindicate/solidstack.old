package solidstack.httpserver;

import java.io.OutputStream;




abstract public class HttpResponse extends Response
{
	private boolean connectionClose;

	@Override
	public void write( OutputStream out )
	{
		ResponseOutputStream out2 = new ResponseOutputStream( out, this.connectionClose );
		write( out2 );
		out2.close();
	}

	abstract public void write( ResponseOutputStream out );

	public void setConnectionClose( boolean connectionClose )
	{
		this.connectionClose = connectionClose;
	}

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
}
