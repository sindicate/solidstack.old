package solidstack.httpserver;


public class CompressionFilter implements Filter
{
	public HttpResponse call( RequestContext context, FilterChain chain )
	{
		final HttpResponse response = chain.call( context );
		return new HttpResponse()
		{
			@Override
			public void write( ResponseOutputStream out )
			{
				out.setHeader( "Content-Encoding", "gzip" );
				out = new GZipResponseOutputStream( out );
				response.write( out );
				out.close();
			}
		};

//		catch( FatalSocketException e )
//		{
//			throw e;
//		}
//		catch( RuntimeException e )
//		{
//			gzipResponse.finish();
//			throw e;
//		}
//		catch( Error e )
//		{
//			gzipResponse.finish();
//			throw e;
//		}
	}
}
