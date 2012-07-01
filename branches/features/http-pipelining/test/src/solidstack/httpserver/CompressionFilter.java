package solidstack.httpserver;

import java.io.IOException;

public class CompressionFilter implements Filter
{
	public Response call( RequestContext context, FilterChain chain )
	{
		final Response response = chain.call( context );
		return new Response()
		{
			@Override
			public void write( ResponseOutputStream out ) throws IOException
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
